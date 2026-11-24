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
    GRASS(true, false, false),

    /** Starting area inside a corner. Behaves like grass. */
    SPAWN(true, false, false),

    /** Impassable water. Arrows, shots and melee reach go over it. */
    RIVER(false, false, false),

    /** The only walkable crossing over a river. */
    BRIDGE(true, false, false),

    /** Heroes can walk in, but ranged shots can't pass through or out of it. Melee still works. */
    FOREST(true, true, false),

    /** Blocks movement, arrows and melee. Permanent for version 1. */
    WALL(false, true, true);

    private final boolean walkable;
    private final boolean blocksArrows;
    private final boolean blocksMelee;

    TileType(boolean walkable, boolean blocksArrows, boolean blocksMelee) {
        this.walkable = walkable;
        this.blocksArrows = blocksArrows;
        this.blocksMelee = blocksMelee;
    }

    /** Whether a piece can stand on this tile. */
    public boolean isWalkable() {
        return walkable;
    }

    /** Whether a ranged shot is stopped by this tile. */
    public boolean blocksArrows() {
        return blocksArrows;
    }

    /** Whether a melee attack with reach (like the Pawn's Knife) is stopped by this tile. */
    public boolean blocksMelee() {
        return blocksMelee;
    }
}
