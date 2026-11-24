package com.legends.board;

import com.legends.model.Direction;
import com.legends.model.Position;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Breadth-first search over the board.
 *
 * <p>Used by the validator now, and later for things like "which squares can this
 * hero reach". The caller decides which steps are allowed (orthogonal only, or all
 * eight directions) and which squares can be entered.
 */
public final class Reachability {

    /** Up, down, left, right. The strictest kind of walking (how the Castle moves). */
    public static final List<Direction> ORTHOGONAL = List.of(Direction.N, Direction.E, Direction.S, Direction.W);

    /** All eight directions (how a king, the Archer and the Pawn move). */
    public static final List<Direction> ALL_DIRECTIONS = List.of(Direction.values());

    private Reachability() {
    }

    /**
     * Returns every square reachable from {@code start} by repeatedly taking one of the
     * given steps onto a square that passes {@code canEnter}. The start square is always
     * included.
     */
    public static Set<Position> reachableFrom(
            Board board, Position start, Collection<Direction> steps, Predicate<Position> canEnter) {
        Set<Position> visited = new LinkedHashSet<>();
        Deque<Position> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            for (Direction step : steps) {
                Position next = current.step(step);
                if (board.contains(next) && !visited.contains(next) && canEnter.test(next)) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }
        return visited;
    }

    /** Convenience: squares reachable by walking orthogonally over walkable tiles. */
    public static Set<Position> walkableFrom(Board board, Position start) {
        return reachableFrom(board, start, ORTHOGONAL, p -> board.tileAt(p).isWalkable());
    }
}
