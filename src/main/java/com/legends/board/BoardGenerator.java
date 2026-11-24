package com.legends.board;

import com.legends.model.Position;
import com.legends.model.Seeds;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Generates a random, fair board from a seed. The rules are in docs/BOARD.md.
 *
 * <p>Steps, in order:
 * <ol>
 *   <li>Randomly assign the corners: 2 river, 1 forest, 1 open.</li>
 *   <li>Build the corners ({@link CornerBuilder}).</li>
 *   <li>Place walls and forest patches ({@link MiddleBuilder}).</li>
 *   <li>Check the result ({@link BoardValidator}). If it fails, start again.</li>
 * </ol>
 *
 * <p>Every random choice comes from one {@link Random} seeded once (via {@link Seeds}),
 * so the same seed always produces the same board, including any retries.
 */
public final class BoardGenerator {

    /** How many boards to try before giving up. Real boards almost always pass in a few tries. */
    public static final int MAX_ATTEMPTS = 500;

    private static final List<CornerType> CORNER_MIX =
            List.of(CornerType.RIVER, CornerType.RIVER, CornerType.FOREST, CornerType.OPEN);

    /** Keeps the board's random numbers separate from other uses of the same seed. */
    private static final long BOARD_SALT = 0x0B0A_4D00_0000_0001L;

    public GeneratedBoard generate(long seed) {
        Random random = Seeds.random(seed, BOARD_SALT);

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            Board board = new Board(BoardLayout.SIZE, BoardLayout.SIZE);
            Map<Corner, CornerType> cornerTypes = assignCorners(random);
            Map<Corner, Position> starts = startSquares();

            new CornerBuilder(board, random).buildAll(cornerTypes);

            MiddleBuilder middle = new MiddleBuilder(board, random, Set.copyOf(starts.values()));
            if (!middle.placeWalls() || !middle.placeForests()) {
                continue;
            }

            if (BoardValidator.validate(board, List.copyOf(starts.values())).isValid()) {
                return new GeneratedBoard(board, seed, attempt, cornerTypes, starts);
            }
        }
        throw new IllegalStateException(
                "Could not generate a valid board from seed " + seed + " in " + MAX_ATTEMPTS + " attempts");
    }

    private Map<Corner, CornerType> assignCorners(Random random) {
        List<CornerType> shuffled = new ArrayList<>(CORNER_MIX);
        Collections.shuffle(shuffled, random);
        Map<Corner, CornerType> types = new EnumMap<>(Corner.class);
        Corner[] corners = Corner.values();
        for (int i = 0; i < corners.length; i++) {
            types.put(corners[i], shuffled.get(i));
        }
        return types;
    }

    private Map<Corner, Position> startSquares() {
        Map<Corner, Position> starts = new EnumMap<>(Corner.class);
        for (Corner corner : Corner.values()) {
            starts.put(corner, BoardLayout.startSquare(corner));
        }
        return starts;
    }
}
