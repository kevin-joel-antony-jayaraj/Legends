package com.legends.board;

import com.legends.model.Offset;
import com.legends.model.Position;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Checks that a board is fair to play on. See "Validity checks" in docs/BOARD.md.
 *
 * <p>Movement is checked orthogonally (up, down, left, right). That's the strictest
 * way any hero walks, so if the Castle can get somewhere, everyone can.
 */
public final class BoardValidator {

    private BoardValidator() {
    }

    public static ValidationResult validate(Board board, List<Position> starts) {
        List<String> problems = new ArrayList<>();

        Set<Position> seen = new HashSet<>();
        for (Position start : starts) {
            if (!seen.add(start)) {
                problems.add("Two heroes start on " + start);
            }
            if (!board.contains(start)) {
                problems.add("Start " + start + " is off the board");
                continue;
            }
            if (!board.tileAt(start).isWalkable()) {
                problems.add("Start " + start + " is on " + board.tileAt(start) + ", which is not walkable");
            } else if (!hasWalkableNeighbour(board, start)) {
                problems.add("Start " + start + " has no walkable square next to it");
            } else if (!hasKnightLanding(board, start)) {
                problems.add("A Knight starting on " + start + " would have nowhere to jump");
            }
        }
        if (!problems.isEmpty() || starts.isEmpty()) {
            return new ValidationResult(problems);
        }

        Set<Position> reachable = Reachability.walkableFrom(board, starts.get(0));
        for (Position start : starts) {
            if (!reachable.contains(start)) {
                problems.add("Start " + start + " can't be reached from " + starts.get(0));
            }
        }
        return new ValidationResult(problems);
    }

    private static boolean hasWalkableNeighbour(Board board, Position p) {
        return Reachability.ORTHOGONAL.stream()
                .map(p::step)
                .anyMatch(n -> isWalkable(board, n));
    }

    /**
     * Any hero might start on any corner, so a Knight must be able to jump out of every
     * start square. A small spawn area with a thick moat can leave every L-jump landing
     * in water.
     */
    private static boolean hasKnightLanding(Board board, Position p) {
        return Offset.KNIGHT_JUMPS.stream()
                .map(jump -> jump.from(p))
                .anyMatch(n -> isWalkable(board, n));
    }

    private static boolean isWalkable(Board board, Position p) {
        return board.contains(p) && board.tileAt(p).isWalkable();
    }
}
