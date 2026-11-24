package com.legends.board;

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
                .anyMatch(n -> board.contains(n) && board.tileAt(n).isWalkable());
    }
}
