package com.legends.board;

/** What a corner contains. Each game has 2 river corners, 1 forest corner and 1 open corner. */
public enum CornerType {
    /** Irregular spawn area surrounded by a river moat, with one bridge. */
    RIVER,
    /** Irregular patch of forest. */
    FOREST,
    /** Plain grass. */
    OPEN
}
