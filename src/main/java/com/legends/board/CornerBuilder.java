package com.legends.board;

import com.legends.model.Position;
import com.legends.model.TileType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Builds the four corners: river moats with bridges, a forest corner and an open corner.
 *
 * <p>All shapes are worked out in local coordinates (see {@link Corner}), so the same
 * code produces every corner. Package-private: only the generator uses it.
 */
final class CornerBuilder {

    /** How far a corner blob stretches in from its edges (docs/BOARD.md: 3 to 5). */
    static final int MIN_STRETCH = 3;
    static final int MAX_STRETCH = 5;

    /**
     * A blob square must have localRow + localCol at most this. It trims the blob's
     * inner corner so that the moat never reaches the open central core.
     */
    private static final int MAX_DIAGONAL_REACH = 5;

    /** Rows 0-2 must be at least 3 wide, so the start square and its neighbours are inside the blob. */
    private static final int ROWS_THAT_MUST_COVER_START = 3;

    /** A square in local coordinates. Kept separate from Position so local and board coordinates can't be mixed up. */
    private record Cell(int row, int col) {
        Cell plus(int dRow, int dCol) {
            return new Cell(row + dRow, col + dCol);
        }
    }

    /** A river corner waiting for its bridge. */
    private record Moat(Set<Cell> blob, Set<Cell> water) {
    }

    private final Board board;
    private final Random random;

    CornerBuilder(Board board, Random random) {
        this.board = board;
        this.random = random;
    }

    /**
     * Builds every corner. All blobs and moats are placed first, then the bridges,
     * so a later corner's river can't paint over an earlier corner's bridge.
     */
    void buildAll(Map<Corner, CornerType> types) {
        Map<Corner, Moat> moats = new EnumMap<>(Corner.class);

        for (Map.Entry<Corner, CornerType> entry : types.entrySet()) {
            Corner corner = entry.getKey();
            switch (entry.getValue()) {
                case OPEN -> {
                    // Plain grass: nothing to build.
                }
                case FOREST -> paint(corner, randomBlob(), TileType.FOREST);
                case RIVER -> {
                    Set<Cell> blob = randomBlob();
                    int width = 1 + random.nextInt(2);
                    Set<Cell> water = moatAround(corner, blob, width);
                    paint(corner, blob, TileType.SPAWN);
                    paint(corner, water, TileType.RIVER);
                    moats.put(corner, new Moat(blob, water));
                }
            }
        }

        moats.forEach(this::placeBridge);
    }

    /**
     * An irregular "staircase" blob tucked into the corner.
     *
     * <p>Each row is no wider than the row above it. That keeps the shape solid
     * (no holes or overhangs), so a straight bridge always leads cleanly out of it.
     */
    private Set<Cell> randomBlob() {
        Set<Cell> blob = new LinkedHashSet<>();
        int depth = between(MIN_STRETCH, MAX_STRETCH);
        int previousWidth = MAX_STRETCH;

        for (int row = 0; row < depth; row++) {
            int widestAllowed = Math.min(previousWidth, Math.min(MAX_STRETCH, MAX_DIAGONAL_REACH - row + 1));
            int narrowestAllowed = row < ROWS_THAT_MUST_COVER_START ? MIN_STRETCH : 1;
            if (widestAllowed < narrowestAllowed) {
                break;
            }
            int width = between(narrowestAllowed, widestAllowed);
            for (int col = 0; col < width; col++) {
                blob.add(new Cell(row, col));
            }
            previousWidth = width;
        }
        return blob;
    }

    /**
     * The water around a blob, {@code width} squares thick.
     *
     * <p>The first ring uses all eight neighbours, so even a diagonal step can't get
     * past it. The second ring is only for looks, so it is left out of the core.
     */
    private Set<Cell> moatAround(Corner corner, Set<Cell> blob, int width) {
        Set<Cell> inside = new LinkedHashSet<>(blob);
        Set<Cell> water = new LinkedHashSet<>();

        for (int ring = 1; ring <= width; ring++) {
            Set<Cell> newRing = new LinkedHashSet<>();
            for (Cell cell : inside) {
                for (int dRow = -1; dRow <= 1; dRow++) {
                    for (int dCol = -1; dCol <= 1; dCol++) {
                        Cell next = cell.plus(dRow, dCol);
                        boolean onBoard = next.row() >= 0 && next.col() >= 0
                                && board.contains(toBoard(corner, next));
                        if (!onBoard || inside.contains(next)) {
                            continue;
                        }
                        boolean outerRingInCore = ring > 1 && BoardLayout.isInCore(toBoard(corner, next));
                        if (!outerRingInCore) {
                            newRing.add(next);
                        }
                    }
                }
            }
            water.addAll(newRing);
            inside.addAll(newRing);
        }
        return water;
    }

    /**
     * Lays one bridge straight out of the blob, toward the middle of the board.
     *
     * <p>Bridges that run along the board's outer edge are only used if there is no
     * other choice. Among the rest, the shortest crossings win, so bridges stay short.
     */
    private void placeBridge(Corner corner, Moat moat) {
        List<List<Cell>> inner = new ArrayList<>();
        List<List<Cell>> alongEdge = new ArrayList<>();

        // Local "down" (+1, 0) and local "right" (0, +1) both point toward the middle.
        int[][] inward = {{1, 0}, {0, 1}};
        for (Cell start : moat.blob()) {
            for (int[] dir : inward) {
                List<Cell> path = new ArrayList<>();
                Cell next = start.plus(dir[0], dir[1]);
                while (moat.water().contains(next)) {
                    path.add(next);
                    next = next.plus(dir[0], dir[1]);
                }
                if (path.isEmpty()) {
                    continue;
                }
                // Going "down" in column 0, or "right" in row 0, hugs the board edge.
                boolean hugsEdge = dir[0] == 1 ? start.col() == 0 : start.row() == 0;
                keepShortest(hugsEdge ? alongEdge : inner, path);
            }
        }

        List<List<Cell>> choices = inner.isEmpty() ? alongEdge : inner;
        if (!choices.isEmpty()) {
            List<Cell> bridge = choices.get(random.nextInt(choices.size()));
            for (Cell cell : bridge) {
                board.setTile(toBoard(corner, cell), TileType.BRIDGE);
            }
        }
    }

    /** Adds the path to the list if it is as short as the shortest so far, dropping longer ones. */
    private static void keepShortest(List<List<Cell>> shortest, List<Cell> path) {
        if (!shortest.isEmpty() && path.size() < shortest.get(0).size()) {
            shortest.clear();
        }
        if (shortest.isEmpty() || path.size() == shortest.get(0).size()) {
            shortest.add(path);
        }
    }

    /** Paints cells onto the board, but only over grass, so corners never overwrite each other. */
    private void paint(Corner corner, Set<Cell> cells, TileType type) {
        for (Cell cell : cells) {
            Position p = toBoard(corner, cell);
            if (board.tileAt(p) == TileType.GRASS) {
                board.setTile(p, type);
            }
        }
    }

    private Position toBoard(Corner corner, Cell cell) {
        return corner.toBoard(cell.row(), cell.col(), board.rows(), board.cols());
    }

    private int between(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}
