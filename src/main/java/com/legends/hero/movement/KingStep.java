package com.legends.hero.movement;

import com.legends.board.BoardView;
import com.legends.model.Direction;
import com.legends.model.Position;
import java.util.LinkedHashSet;
import java.util.Set;

/** One square in any of the eight directions, like a chess king. Used by the Archer and the Pawn. */
public final class KingStep implements MovementPattern {

    @Override
    public Set<Position> destinations(Position from, BoardView view) {
        Set<Position> result = new LinkedHashSet<>();
        for (Direction direction : Direction.values()) {
            Position next = from.step(direction);
            if (view.isFreeToEnter(next)) {
                result.add(next);
            }
        }
        return result;
    }
}
