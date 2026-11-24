package com.legends.model;

/**
 * The eight directions on the grid. Row grows downward, column grows rightward.
 *
 * <p>Each direction is a fixed offset, so the same table can be reused by every
 * piece that moves or attacks in straight lines.
 */
public enum Direction {
    N(-1, 0),
    NE(-1, 1),
    E(0, 1),
    SE(1, 1),
    S(1, 0),
    SW(1, -1),
    W(0, -1),
    NW(-1, -1);

    private final int dRow;
    private final int dCol;

    Direction(int dRow, int dCol) {
        this.dRow = dRow;
        this.dCol = dCol;
    }

    public int dRow() {
        return dRow;
    }

    public int dCol() {
        return dCol;
    }

    /** True for N, E, S and W. */
    public boolean isOrthogonal() {
        return dRow == 0 || dCol == 0;
    }

    /** True for NE, SE, SW and NW. */
    public boolean isDiagonal() {
        return !isOrthogonal();
    }
}
