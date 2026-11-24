package com.legends.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.legends.model.Position;
import com.legends.model.TileType;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BoardGeneratorTest {

    /** Enough seeds to catch rare bad layouts, still fast. */
    private static final int SEEDS_TO_CHECK = 500;

    private final BoardGenerator generator = new BoardGenerator();

    @Test
    void sameSeedGivesSameBoard() {
        GeneratedBoard first = generator.generate(42);
        GeneratedBoard second = generator.generate(42);
        assertEquals(BoardRenderer.render(first.board()), BoardRenderer.render(second.board()));
        assertEquals(first.cornerTypes(), second.cornerTypes());
    }

    @Test
    void differentSeedsGiveDifferentBoards() {
        assertNotEquals(
                BoardRenderer.render(generator.generate(1).board()),
                BoardRenderer.render(generator.generate(2).board()));
    }

    @Test
    void everyBoardPassesValidation() {
        for (long seed = 0; seed < SEEDS_TO_CHECK; seed++) {
            GeneratedBoard g = generator.generate(seed);
            ValidationResult result = BoardValidator.validate(g.board(), g.startList());
            assertTrue(result.isValid(), "seed " + seed + ": " + result.problems());
        }
    }

    @Test
    void cornersAreTwoRiverOneForestOneOpen() {
        for (long seed = 0; seed < SEEDS_TO_CHECK; seed++) {
            var types = generator.generate(seed).cornerTypes().values();
            assertEquals(2, Collections.frequency(types, CornerType.RIVER), "seed " + seed);
            assertEquals(1, Collections.frequency(types, CornerType.FOREST), "seed " + seed);
            assertEquals(1, Collections.frequency(types, CornerType.OPEN), "seed " + seed);
        }
    }

    @Test
    void heroesStartOneSquareInFromEachCorner() {
        GeneratedBoard g = generator.generate(7);
        assertEquals(
                Set.of(new Position(1, 1), new Position(1, 10), new Position(10, 1), new Position(10, 10)),
                Set.copyOf(g.startList()));
    }

    @Test
    void startSquareMatchesItsCornerType() {
        for (long seed = 0; seed < SEEDS_TO_CHECK; seed++) {
            GeneratedBoard g = generator.generate(seed);
            for (Map.Entry<Corner, CornerType> e : g.cornerTypes().entrySet()) {
                TileType startTile = g.board().tileAt(g.starts().get(e.getKey()));
                TileType expected = switch (e.getValue()) {
                    case RIVER -> TileType.SPAWN;
                    case FOREST -> TileType.FOREST;
                    case OPEN -> TileType.GRASS;
                };
                assertEquals(expected, startTile, "seed " + seed + ", " + e.getKey());
            }
        }
    }

    @Test
    void coreStaysOpen() {
        for (long seed = 0; seed < SEEDS_TO_CHECK; seed++) {
            Board board = generator.generate(seed).board();
            int coreWalls = 0;
            for (int r = BoardLayout.CORE_FIRST; r <= BoardLayout.CORE_LAST; r++) {
                for (int c = BoardLayout.CORE_FIRST; c <= BoardLayout.CORE_LAST; c++) {
                    TileType t = board.tileAt(new Position(r, c));
                    assertTrue(t == TileType.GRASS || t == TileType.WALL,
                            "seed " + seed + ": core square (" + r + "," + c + ") is " + t);
                    if (t == TileType.WALL) {
                        coreWalls++;
                    }
                }
            }
            assertTrue(coreWalls <= MiddleBuilder.MAX_WALL_LENGTH,
                    "seed " + seed + ": more than one wall segment in the core");
        }
    }

    @Test
    void wallTileCountMatchesThreeToFiveSegments() {
        for (long seed = 0; seed < SEEDS_TO_CHECK; seed++) {
            int walls = count(generator.generate(seed).board(), TileType.WALL);
            assertTrue(walls >= MiddleBuilder.MIN_WALLS * MiddleBuilder.MIN_WALL_LENGTH
                            && walls <= MiddleBuilder.MAX_WALLS * MiddleBuilder.MAX_WALL_LENGTH,
                    "seed " + seed + ": " + walls + " wall tiles");
        }
    }

    /**
     * The moat must really enclose the spawn area. With bridges treated as water, a
     * king-style walker (all 8 directions) must not be able to leave.
     */
    @Test
    void riverCornersCanOnlyBeLeftByTheBridge() {
        for (long seed = 0; seed < SEEDS_TO_CHECK; seed++) {
            GeneratedBoard g = generator.generate(seed);
            Board board = g.board();
            for (Map.Entry<Corner, CornerType> e : g.cornerTypes().entrySet()) {
                if (e.getValue() != CornerType.RIVER) {
                    continue;
                }
                Position start = g.starts().get(e.getKey());
                Set<Position> withoutBridge = Reachability.reachableFrom(board, start, Reachability.ALL_DIRECTIONS,
                        p -> board.tileAt(p).isWalkable() && board.tileAt(p) != TileType.BRIDGE);
                for (Position p : withoutBridge) {
                    assertEquals(TileType.SPAWN, board.tileAt(p),
                            "seed " + seed + ", " + e.getKey() + ": escaped the moat at " + p);
                }
            }
        }
    }

    @Test
    void cornerBlobsStretchThreeToFiveSquares() {
        for (long seed = 0; seed < SEEDS_TO_CHECK; seed++) {
            GeneratedBoard g = generator.generate(seed);
            for (Map.Entry<Corner, CornerType> e : g.cornerTypes().entrySet()) {
                if (e.getValue() == CornerType.OPEN) {
                    continue;
                }
                TileType blobTile = e.getValue() == CornerType.RIVER ? TileType.SPAWN : TileType.FOREST;
                int along = run(g.board(), e.getKey(), blobTile, false);
                int down = run(g.board(), e.getKey(), blobTile, true);
                String where = "seed " + seed + ", " + e.getKey();
                assertTrue(along >= CornerBuilder.MIN_STRETCH, where + ": edge run " + along);
                assertTrue(down >= CornerBuilder.MIN_STRETCH, where + ": edge run " + down);
                // Spawn areas are bounded by the moat. Forest corners can merge with a
                // middle forest patch, so only the river corners get an upper bound.
                if (e.getValue() == CornerType.RIVER) {
                    assertTrue(along <= CornerBuilder.MAX_STRETCH, where + ": edge run " + along);
                    assertTrue(down <= CornerBuilder.MAX_STRETCH, where + ": edge run " + down);
                }
            }
        }
    }

    @Test
    void everyRiverCornerHasABridge() {
        for (long seed = 0; seed < SEEDS_TO_CHECK; seed++) {
            GeneratedBoard g = generator.generate(seed);
            long rivers = g.cornerTypes().values().stream().filter(t -> t == CornerType.RIVER).count();
            assertFalse(rivers > 0 && count(g.board(), TileType.BRIDGE) == 0, "seed " + seed);
        }
    }

    /** Counts matching tiles in a straight line along the corner's edge, starting at the corner. */
    private static int run(Board board, Corner corner, TileType tile, boolean downTheSide) {
        int length = 0;
        for (int i = 0; i < board.rows(); i++) {
            Position p = downTheSide
                    ? corner.toBoard(i, 0, board.rows(), board.cols())
                    : corner.toBoard(0, i, board.rows(), board.cols());
            if (board.tileAt(p) != tile) {
                break;
            }
            length++;
        }
        return length;
    }

    private static int count(Board board, TileType tile) {
        int n = 0;
        for (int r = 0; r < board.rows(); r++) {
            for (int c = 0; c < board.cols(); c++) {
                if (board.tileAt(new Position(r, c)) == tile) {
                    n++;
                }
            }
        }
        return n;
    }
}
