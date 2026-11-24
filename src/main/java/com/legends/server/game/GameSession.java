package com.legends.server.game;

import com.legends.engine.Action;
import com.legends.engine.GameEngine;
import com.legends.engine.event.GameEvent;
import com.legends.server.view.GameView;
import com.legends.server.view.PlayerViews;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

/**
 * One running game on the server: the engine plus a secret token for each seat.
 *
 * <p>The token is how a player proves which seat they are. Whoever has a seat's token can
 * act for that seat, so it should only be shared with that player.
 *
 * <p>Methods are {@code synchronized}: two players' requests can arrive at the same moment
 * on different server threads, and the engine must handle them one at a time.
 */
final class GameSession {

    private final String id;
    private final GameEngine engine;
    private final Map<String, Integer> seatByToken;

    GameSession(String id, GameEngine engine, Map<String, Integer> seatByToken) {
        this.id = id;
        this.engine = engine;
        this.seatByToken = Map.copyOf(seatByToken);
    }

    String id() {
        return id;
    }

    OptionalInt seatFor(String token) {
        Integer seat = token == null ? null : seatByToken.get(token);
        return seat == null ? OptionalInt.empty() : OptionalInt.of(seat);
    }

    synchronized GameView viewFor(int seat) {
        return PlayerViews.forSeat(id, engine.game(), seat);
    }

    synchronized List<GameEvent> perform(Action action) {
        return engine.perform(action);
    }
}
