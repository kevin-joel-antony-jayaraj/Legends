package com.legends.board;

import com.legends.model.Position;

/**
 * A read-only look at the board plus which squares are occupied.
 *
 * <p>Movement and attack rules only need this much, so they depend on this small
 * interface instead of the whole game state. That keeps them easy to test: a test
 * can build a BoardView from a bare Board and a set of occupied squares.
 */
public interface BoardView {

    Board board();

    boolean isOccupied(Position p);

    /** True if a piece could step onto this square: on the board, walkable, and empty. */
    default boolean isFreeToEnter(Position p) {
        return board().contains(p) && board().tileAt(p).isWalkable() && !isOccupied(p);
    }
}
