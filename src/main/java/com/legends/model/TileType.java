package com.legends.model;

/**
 * The kinds of terrain a square can have.
 *
 * <p>The rules live here, next to the data they describe. A tile says what it
 * allows, and the rest of the game asks the tile rather than checking names.
 * Display symbols are NOT stored here: rendering belongs to the view layer.
 */
public enum TileType {

    /** Plain ground. Walkable, does not block arrows. */
    GRASS(true, false),

    /** Starting area inside a corner. Behaves like grass. */
    SPAWN(true, false),

    /** Impassable water. Arrows and shots fly over it. */
    RIVER(false, false),

    /** The only walkable crossing over a river. */
    BRIDGE(true, false),

    /** Heroes can walk in, but ranged shots can't pass through or out of it. */
    FOREST(true, true),

    /** Blocks movement and arrows. Permanent for version 1. */
    WALL(false, true);

    private final boolean walkable;
    private final boolean blocksArrows;

    TileType(boolean walkable, boolean blocksArrows) {
        this.walkable = walkable;
        this.blocksArrows = blocksArrows;
    }

    /** Whether a piece can stand on this tile. */
    public boolean isWalkable() {
        return walkable;
    }

    /** Whether a ranged shot is stopped by this tile. */
    public boolean blocksArrows() {
        return blocksArrows;
    }
}
