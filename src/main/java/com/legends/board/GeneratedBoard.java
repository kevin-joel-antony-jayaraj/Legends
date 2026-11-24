package com.legends.board;

import com.legends.model.Position;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The result of generating a board: the board itself plus everything needed to
 * understand or reproduce it.
 *
 * @param board       the generated terrain
 * @param seed        the seed that produced it; the same seed always gives the same board
 * @param attempts    how many tries the generator needed (useful when debugging)
 * @param cornerTypes what each corner became
 * @param starts      the hero start square for each corner
 */
public record GeneratedBoard(
        Board board,
        long seed,
        int attempts,
        Map<Corner, CornerType> cornerTypes,
        Map<Corner, Position> starts) {

    public GeneratedBoard {
        cornerTypes = Collections.unmodifiableMap(new EnumMap<>(cornerTypes));
        starts = Collections.unmodifiableMap(new EnumMap<>(starts));
    }

    /** Start squares in corner order (top-left, top-right, bottom-left, bottom-right). */
    public List<Position> startList() {
        return List.copyOf(starts.values());
    }
}
