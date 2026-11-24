package com.legends.board;

import com.legends.model.Position;

/**
 * Fixed layout facts shared by the generator, the validator and the tests.
 *
 * <p>Keeping these numbers in one place means a rule like "the core is rows 4-7"
 * can't drift out of sync between classes.
 */
public final class BoardLayout {

    /** The board is SIZE x SIZE. */
    public static final int SIZE = 12;

    /** Heroes start this many squares in from their corner, diagonally. */
    public static final int START_INSET = 1;

    /** First and last row/column of the open central core (inclusive). */
    public static final int CORE_FIRST = 4;
    public static final int CORE_LAST = 7;

    private BoardLayout() {
    }

    /** True if the position is inside the open central core. */
    public static boolean isInCore(Position p) {
        return p.row() >= CORE_FIRST && p.row() <= CORE_LAST
                && p.col() >= CORE_FIRST && p.col() <= CORE_LAST;
    }

    /** The square where the hero for this corner starts. */
    public static Position startSquare(Corner corner) {
        return corner.toBoard(START_INSET, START_INSET, SIZE, SIZE);
    }
}
