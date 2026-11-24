package com.legends.server.game;

import com.legends.engine.Action;
import com.legends.engine.GameEngine;
import com.legends.engine.event.GameEvent;
import com.legends.game.GameSetup;
import com.legends.game.GameState;
import com.legends.game.PlayerChoice;
import com.legends.server.view.EventView;
import com.legends.server.view.GameView;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Creates games, checks who is asking, and runs their actions through the engine.
 *
 * <p>Games are kept in memory, so they disappear when the server restarts. Saving them
 * to a database is a later step.
 */
@Service
public class GameService {

    /** A seat's details as returned once, when the game is created. */
    public record SeatTicket(int seat, String name, String hero, String token) {
    }

    /** What the creator gets back: the game's id, its seed, and every seat's token. */
    public record CreatedGame(String gameId, long seed, List<SeatTicket> seats) {
    }

    /** What a player gets back after acting: what happened, and their updated view. */
    public record ActionOutcome(List<EventView> events, GameView view) {
    }

    private final Map<String, GameSession> games = new ConcurrentHashMap<>();
    private final GameSetup setup = new GameSetup();
    private final SecureRandom seeds = new SecureRandom();
    private final EventPublisher publisher;

    public GameService(EventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Starts a game.
     *
     * @param seed    the board seed, or null for a random one
     * @param players 2 to 4 players in clockwise seat order
     */
    public CreatedGame create(Long seed, List<PlayerChoice> players) {
        long actualSeed = seed != null ? seed : seeds.nextLong();
        GameState state = setup.newGame(actualSeed, players);

        String gameId = UUID.randomUUID().toString();
        Map<String, Integer> seatByToken = new HashMap<>();
        List<SeatTicket> tickets = new ArrayList<>();
        for (int seat = 0; seat < players.size(); seat++) {
            String token = UUID.randomUUID().toString();
            seatByToken.put(token, seat);
            PlayerChoice choice = players.get(seat);
            tickets.add(new SeatTicket(seat, choice.name(), choice.hero().name(), token));
        }
        games.put(gameId, new GameSession(gameId, new GameEngine(state), seatByToken));
        return new CreatedGame(gameId, actualSeed, tickets);
    }

    /** The game as the token's player may see it. */
    public GameView view(String gameId, String token) {
        GameSession session = session(gameId);
        return session.viewFor(seat(session, token));
    }

    /**
     * Performs an action for the token's player and tells everyone what happened.
     *
     * @throws com.legends.engine.IllegalActionException if the rules don't allow it
     */
    public ActionOutcome act(String gameId, String token, ActionFactory request) {
        GameSession session = session(gameId);
        int seat = seat(session, token);
        Action action = request.toAction(seat);

        List<GameEvent> events = session.perform(action);
        List<EventView> views = events.stream().map(EventView::of).toList();
        publisher.publish(gameId, views);
        return new ActionOutcome(views, session.viewFor(seat));
    }

    /** Anything that can turn into an engine action once we know whose seat it's for. */
    public interface ActionFactory {
        Action toAction(int seat);
    }

    private GameSession session(String gameId) {
        GameSession session = games.get(gameId);
        if (session == null) {
            throw new GameNotFoundException(gameId);
        }
        return session;
    }

    private static int seat(GameSession session, String token) {
        return session.seatFor(token).orElseThrow(NotAPlayerException::new);
    }
}
