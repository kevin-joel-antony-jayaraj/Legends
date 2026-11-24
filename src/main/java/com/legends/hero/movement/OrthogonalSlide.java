package com.legends.hero.movement;

import com.legends.board.BoardView;
import com.legends.model.Direction;
import com.legends.model.Position;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Slides in a straight line up, down, left or right, like a chess rook, up to a
 * maximum distance. Used by the Castle (3 squares).
 *
 * <p>Unlike the Knight, a slide can't pass through anything: it stops at the first
 * river, wall or piece in the way.
 */
public final class OrthogonalSlide implements MovementPattern {

    private static final List<Direction> ORTHOGONAL = List.of(Direction.N, Direction.E, Direction.S, Direction.W);

    private final int maxDistance;

    public OrthogonalSlide(int maxDistance) {
        if (maxDistance <= 0) {
            throw new IllegalArgumentException("Max distance must be positive, got " + maxDistance);
        }
        this.maxDistance = maxDistance;
    }

    public int maxDistance() {
        return maxDistance;
    }

    @Override
    public Set<Position> destinations(Position from, BoardView view) {
        Set<Position> result = new LinkedHashSet<>();
        for (Direction direction : ORTHOGONAL) {
            Position next = from;
            for (int distance = 1; distance <= maxDistance; distance++) {
                next = next.step(direction);
                if (!view.isFreeToEnter(next)) {
                    break;
                }
                result.add(next);
            }
        }
        return result;
    }
}
