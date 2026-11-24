package com.legends.game;

import com.legends.board.BoardGenerator;
import com.legends.board.Corner;
import com.legends.board.GeneratedBoard;
import com.legends.hero.Hero;
import com.legends.hero.Player;
import com.legends.model.Position;
import com.legends.model.Seeds;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates new games. This is the only way to build a {@link GameState}, so every game
 * starts valid: a generated board, each hero on its corner, and a random first player.
 */
public final class GameSetup {

    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 4;

    /** Corners in clockwise order. Seat 0 gets the first, seat 1 the next, and so on. */
    public static final List<Corner> CLOCKWISE =
            List.of(Corner.TOP_LEFT, Corner.TOP_RIGHT, Corner.BOTTOM_RIGHT, Corner.BOTTOM_LEFT);

    /** Keeps setup's random numbers (who goes first) separate from the board generator's. */
    private static final long SETUP_SALT = 0x9E3779B97F4A7C15L;

    private final BoardGenerator boardGenerator;

    public GameSetup() {
        this(new BoardGenerator());
    }

    public GameSetup(BoardGenerator boardGenerator) {
        this.boardGenerator = boardGenerator;
    }

    /**
     * Starts a new game. The same seed and players always give the same game.
     *
     * @param players in clockwise seat order; 2 to 4 players (4 is the real game,
     *                fewer is handy for testing)
     */
    public GameState newGame(long seed, List<PlayerChoice> players) {
        if (players.size() < MIN_PLAYERS || players.size() > MAX_PLAYERS) {
            throw new IllegalArgumentException(
                    "Need " + MIN_PLAYERS + " to " + MAX_PLAYERS + " players, got " + players.size());
        }

        GeneratedBoard generated = boardGenerator.generate(seed);

        List<Hero> seats = new ArrayList<>();
        List<Position> starts = new ArrayList<>();
        for (int seat = 0; seat < players.size(); seat++) {
            PlayerChoice choice = players.get(seat);
            seats.add(choice.hero().create(new Player(seat, choice.name())));
            starts.add(generated.starts().get(CLOCKWISE.get(seat)));
        }

        int firstSeat = Seeds.random(seed, SETUP_SALT).nextInt(players.size());
        return new GameState(generated.board(), seed, seats, starts, firstSeat);
    }
}
