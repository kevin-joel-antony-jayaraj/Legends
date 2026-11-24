package com.legends.board;

import com.legends.model.Position;
import com.legends.model.TileType;

/**
 * A rectangular grid of terrain tiles.
 *
 * <p>The Board only knows about terrain. It does not know about pieces, players
 * or turns, so it can be tested on its own and reused on the server later.
 *
 * <p>Coordinates are always checked here, so no other class needs to worry about
 * running off the edge.
 */
public final class Board {

    private final int rows;
    private final int cols;
    private final TileType[][] tiles;

    /** Creates a board filled with grass. */
    public Board(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Board must have at least 1 row and 1 column");
        }
        this.rows = rows;
        this.cols = cols;
        this.tiles = new TileType[rows][cols];
        for (TileType[] row : tiles) {
            java.util.Arrays.fill(row, TileType.GRASS);
        }
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    /** True if the position is inside the board. */
    public boolean contains(Position p) {
        return p.row() >= 0 && p.row() < rows && p.col() >= 0 && p.col() < cols;
    }

    /** Returns the terrain at a position. Throws if the position is off the board. */
    public TileType tileAt(Position p) {
        requireInside(p);
        return tiles[p.row()][p.col()];
    }

    /** Sets the terrain at a position. Throws if the position is off the board. */
    public void setTile(Position p, TileType type) {
        requireInside(p);
        if (type == null) {
            throw new IllegalArgumentException("Tile type cannot be null");
        }
        tiles[p.row()][p.col()] = type;
    }

    private void requireInside(Position p) {
        if (!contains(p)) {
            throw new IllegalArgumentException("Position " + p + " is outside the " + rows + "x" + cols + " board");
        }
    }
}
