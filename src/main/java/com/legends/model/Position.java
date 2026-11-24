package com.legends.model;

/**
 * A square on the board, identified by row and column (both 0-based).
 *
 * <p>A record is immutable, so a Position can be safely shared and used as a
 * key in maps and sets. It has no game logic: whether a square is valid is
 * the Board's job, not the Position's.
 */
public record Position(int row, int col) {

    /** Returns the square one step away in the given direction. */
    public Position step(Direction direction) {
        return new Position(row + direction.dRow(), col + direction.dCol());
    }

    /** Returns the square offset by the given amounts. Used for knight jumps and similar moves. */
    public Position offset(int dRow, int dCol) {
        return new Position(row + dRow, col + dCol);
    }

    /**
     * How many king steps apart two squares are, counting a diagonal step as 1.
     * "Within 2 squares" in the rules means a distance of 2 or less.
     */
    public int kingDistance(Position other) {
        return Math.max(Math.abs(row - other.row), Math.abs(col - other.col));
    }

    /** Prints as (row,col), so messages and test failures are easy to read. */
    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }
}
