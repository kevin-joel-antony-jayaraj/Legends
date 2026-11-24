package com.legends.board;

import com.legends.model.Position;

/**
 * The four corners of the board.
 *
 * <p>Corner features are designed once in "local" coordinates, where (0, 0) is the
 * corner square and both numbers grow toward the middle of the board. Each corner
 * then maps local coordinates onto the real board by flipping rows and/or columns.
 * This way one piece of code builds all four corners.
 */
public enum Corner {
    TOP_LEFT(false, false),
    TOP_RIGHT(false, true),
    BOTTOM_LEFT(true, false),
    BOTTOM_RIGHT(true, true);

    private final boolean flipRow;
    private final boolean flipCol;

    Corner(boolean flipRow, boolean flipCol) {
        this.flipRow = flipRow;
        this.flipCol = flipCol;
    }

    /**
     * Converts a local position (distance from this corner) to a board position.
     *
     * @param localRow how many rows in from this corner's edge
     * @param localCol how many columns in from this corner's edge
     */
    public Position toBoard(int localRow, int localCol, int boardRows, int boardCols) {
        int row = flipRow ? boardRows - 1 - localRow : localRow;
        int col = flipCol ? boardCols - 1 - localCol : localCol;
        return new Position(row, col);
    }
}
