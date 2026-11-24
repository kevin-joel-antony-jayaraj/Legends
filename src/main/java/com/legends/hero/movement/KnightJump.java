package com.legends.hero.movement;

import com.legends.board.BoardView;
import com.legends.model.Offset;
import com.legends.model.Position;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The chess knight's L-jump: 2 squares one way and 1 to the side.
 *
 * <p>Only the landing square matters. The Knight jumps over pieces, rivers and walls,
 * but must land on a free, walkable square. Stopping early along the L is part of the
 * Knight's "Check" attack, not plain movement.
 */
public final class KnightJump implements MovementPattern {

    @Override
    public Set<Position> destinations(Position from, BoardView view) {
        Set<Position> result = new LinkedHashSet<>();
        for (Offset jump : Offset.KNIGHT_JUMPS) {
            Position landing = jump.from(from);
            if (view.isFreeToEnter(landing)) {
                result.add(landing);
            }
        }
        return result;
    }
}
