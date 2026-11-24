package com.legends.board;

import com.legends.model.Position;
import com.legends.model.TileType;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Places random walls and forest patches after the corners are built.
 *
 * <p>Both only go onto grass, so they never cover a river, bridge or spawn area, but
 * they are allowed to touch corner blobs. Package-private: only the generator uses it.
 */
final class MiddleBuilder {

    static final int MIN_WALLS = 3;
    static final int MAX_WALLS = 5;
    static final int MIN_WALL_LENGTH = 1;
    static final int MAX_WALL_LENGTH = 4;

    static final int MIN_FOREST_PATCHES = 2;
    static final int MAX_FOREST_PATCHES = 4;
    static final int MIN_FOREST_SIZE = 2;
    static final int MAX_FOREST_SIZE = 4;

    /** How many random spots to try per wall or patch before giving up on this board. */
    private static final int TRIES_PER_FEATURE = 100;

    private final Board board;
    private final Random random;
    private final Set<Position> reserved;

    /**
     * @param reserved squares that must stay clear, such as hero start squares
     */
    MiddleBuilder(Board board, Random random, Set<Position> reserved) {
        this.board = board;
        this.random = random;
        this.reserved = Set.copyOf(reserved);
    }

    /**
     * Places 3-5 straight wall segments. At most one segment may enter the core.
     *
     * @return false if the walls didn't fit, so the generator should start over
     */
    boolean placeWalls() {
        int count = between(MIN_WALLS, MAX_WALLS);
        boolean coreWallPlaced = false;

        for (int wall = 0; wall < count; wall++) {
            boolean placed = false;
            for (int attempt = 0; attempt < TRIES_PER_FEATURE && !placed; attempt++) {
                List<Position> segment = randomSegment();
                if (!segment.stream().allMatch(this::isFreeGrass)) {
                    continue;
                }
                boolean entersCore = segment.stream().anyMatch(BoardLayout::isInCore);
                if (entersCore && coreWallPlaced) {
                    continue;
                }
                segment.forEach(p -> board.setTile(p, TileType.WALL));
                coreWallPlaced |= entersCore;
                placed = true;
            }
            if (!placed) {
                return false;
            }
        }
        return true;
    }

    /**
     * Places 2-4 irregular forest patches of 2-4 squares each, outside the core.
     *
     * @return false if the patches didn't fit, so the generator should start over
     */
    boolean placeForests() {
        int count = between(MIN_FOREST_PATCHES, MAX_FOREST_PATCHES);
        for (int patch = 0; patch < count; patch++) {
            boolean placed = false;
            for (int attempt = 0; attempt < TRIES_PER_FEATURE && !placed; attempt++) {
                placed = tryGrowForest(between(MIN_FOREST_SIZE, MAX_FOREST_SIZE));
            }
            if (!placed) {
                return false;
            }
        }
        return true;
    }

    private List<Position> randomSegment() {
        int length = between(MIN_WALL_LENGTH, MAX_WALL_LENGTH);
        boolean horizontal = random.nextBoolean();
        Position origin = randomPosition();
        List<Position> segment = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            segment.add(horizontal ? origin.offset(0, i) : origin.offset(i, 0));
        }
        return segment;
    }

    /** Grows a patch one square at a time from a random seed square, so the shape is irregular. */
    private boolean tryGrowForest(int size) {
        Position seed = randomPosition();
        if (!isForestable(seed)) {
            return false;
        }
        Set<Position> patch = new LinkedHashSet<>();
        patch.add(seed);

        while (patch.size() < size) {
            List<Position> frontier = new ArrayList<>();
            for (Position p : patch) {
                for (var step : Reachability.ORTHOGONAL) {
                    Position next = p.step(step);
                    if (!patch.contains(next) && !frontier.contains(next) && isForestable(next)) {
                        frontier.add(next);
                    }
                }
            }
            if (frontier.isEmpty()) {
                return false;
            }
            patch.add(frontier.get(random.nextInt(frontier.size())));
        }
        patch.forEach(p -> board.setTile(p, TileType.FOREST));
        return true;
    }

    private boolean isFreeGrass(Position p) {
        return board.contains(p) && board.tileAt(p) == TileType.GRASS && !reserved.contains(p);
    }

    private boolean isForestable(Position p) {
        return isFreeGrass(p) && !BoardLayout.isInCore(p);
    }

    private Position randomPosition() {
        return new Position(random.nextInt(board.rows()), random.nextInt(board.cols()));
    }

    private int between(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}
