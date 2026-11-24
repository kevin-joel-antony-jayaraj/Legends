package com.legends.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TileTypeTest {

    @Test
    void riverIsNotWalkableButDoesNotBlockArrows() {
        assertFalse(TileType.RIVER.isWalkable());
        assertFalse(TileType.RIVER.blocksArrows());
    }

    @Test
    void bridgeIsWalkable() {
        assertTrue(TileType.BRIDGE.isWalkable());
    }

    @Test
    void forestIsWalkableButBlocksArrows() {
        assertTrue(TileType.FOREST.isWalkable());
        assertTrue(TileType.FOREST.blocksArrows());
    }

    @Test
    void wallBlocksMovementAndArrows() {
        assertFalse(TileType.WALL.isWalkable());
        assertTrue(TileType.WALL.blocksArrows());
    }
}
