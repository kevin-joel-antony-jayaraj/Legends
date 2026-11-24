package com.legends.model;

import java.util.List;

/**
 * A fixed jump from one square to another, as a row and column difference.
 *
 * <p>Used for moves that aren't a straight line, like the Knight's L. The list lives
 * here so the Knight's movement and the board validator use the exact same jumps.
 */
public record Offset(int dRow, int dCol) {

    /** The eight L-shaped chess knight jumps: 2 squares one way, 1 to the side. */
    public static final List<Offset> KNIGHT_JUMPS = List.of(
            new Offset(-2, -1), new Offset(-2, 1),
            new Offset(-1, -2), new Offset(-1, 2),
            new Offset(1, -2), new Offset(1, 2),
            new Offset(2, -1), new Offset(2, 1));

    /** The square this offset leads to from {@code from}. */
    public Position from(Position from) {
        return from.offset(dRow, dCol);
    }
}
