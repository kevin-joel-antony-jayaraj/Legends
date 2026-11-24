package com.legends.model;

import java.util.List;

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

    /** N, E, S, W: the rook's directions. */
    public static final List<Direction> ORTHOGONALS = List.of(N, E, S, W);

    /** NE, SE, SW, NW: the bishop's directions. */
    public static final List<Direction> DIAGONALS = List.of(NE, SE, SW, NW);

    /** All eight: the queen's and king's directions. */
    public static final List<Direction> ALL = List.of(values());

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

    /** The two directions at right angles to this one (for N: W and E). Only for orthogonal directions. */
    public List<Direction> perpendiculars() {
        if (!isOrthogonal()) {
            throw new IllegalStateException(this + " is diagonal");
        }
        return dRow == 0 ? List.of(N, S) : List.of(W, E);
    }
}
