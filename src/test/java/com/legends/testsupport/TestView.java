package com.legends.testsupport;

import com.legends.board.Board;
import com.legends.board.BoardView;
import com.legends.model.Position;
import com.legends.model.TileType;
import java.util.HashSet;
import java.util.Set;

/**
 * A tiny board for tests: terrain you set by hand plus a set of occupied squares.
 * Lets movement and attack rules be tested without a whole game.
 */
public final class TestView implements BoardView {

    private final Board board;
    private final Set<Position> occupied = new HashSet<>();

    public TestView(int size) {
        this.board = new Board(size, size);
    }

    public TestView tile(int row, int col, TileType type) {
        board.setTile(new Position(row, col), type);
        return this;
    }

    public TestView occupy(int row, int col) {
        occupied.add(new Position(row, col));
        return this;
    }

    @Override
    public Board board() {
        return board;
    }

    @Override
    public boolean isOccupied(Position p) {
        return occupied.contains(p);
    }

    /** Shorthand for {@code new Position(row, col)}. */
    public static Position at(int row, int col) {
        return new Position(row, col);
    }
}
