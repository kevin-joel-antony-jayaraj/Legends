package com.legends.board;

import com.legends.model.Position;
import com.legends.model.TileType;
import java.util.Map;

/**
 * Turns a Board into text for the console.
 *
 * <p>This is a view. It reads the board and draws it, and it never changes game
 * state. A browser renderer later will do the same job for the canvas.
 */
public final class BoardRenderer {

    private BoardRenderer() {
        // Utility class: no instances.
    }

    /** Draws the board as text, with row and column numbers along the edges. */
    public static String render(Board board) {
        return render(board, Map.of());
    }

    /**
     * Draws the board with some squares replaced by a symbol, for example 'S' for
     * start squares now, or hero letters later.
     */
    public static String render(Board board, Map<Position, Character> overlay) {
        StringBuilder out = new StringBuilder();
        out.append("    ");
        for (int c = 0; c < board.cols(); c++) {
            out.append(String.format("%2d ", c));
        }
        out.append('\n');

        for (int r = 0; r < board.rows(); r++) {
            out.append(String.format("%2d  ", r));
            for (int c = 0; c < board.cols(); c++) {
                Position p = new Position(r, c);
                char symbol = overlay.getOrDefault(p, symbolFor(board.tileAt(p)));
                out.append(' ').append(symbol).append(' ');
            }
            out.append('\n');
        }
        return out.toString();
    }

    /** One-character symbol for each tile. Kept here, not in TileType, so the model stays free of display details. */
    static char symbolFor(TileType type) {
        return switch (type) {
            case GRASS -> '.';
            case SPAWN -> 'P';
            case RIVER -> '~';
            case BRIDGE -> '=';
            case FOREST -> 'F';
            case WALL -> '#';
        };
    }
}
