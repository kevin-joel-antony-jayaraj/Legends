package com.legends.hero.attack;

import com.legends.board.BoardView;
import com.legends.model.Direction;
import com.legends.model.Position;
import com.legends.model.TileType;
import java.util.Optional;

/**
 * Shared line-of-sight rules for attacks that travel in a straight line.
 *
 * <p>Ranged attacks (arrows, shots) stop at forest and walls, and can't be fired from
 * inside a forest. Melee attacks with reach (like the Knife) stop only at walls. Both
 * pass over rivers. See docs/BOARD.md.
 */
final class Lines {

    /** The first piece found along a line, and how far away it is. */
    record Found(Position target, int distance) {
    }

    private Lines() {
    }

    /**
     * Walks from {@code from} in one direction and returns the first occupied square,
     * or empty if the line is blocked, leaves the board, or runs out of range.
     */
    static Optional<Found> firstPiece(BoardView view, Position from, Direction direction, int maxRange, boolean ranged) {
        Position square = from;
        for (int distance = 1; distance <= maxRange; distance++) {
            square = square.step(direction);
            if (!view.board().contains(square)) {
                return Optional.empty();
            }
            TileType tile = view.board().tileAt(square);
            if (ranged ? tile.blocksArrows() : tile.blocksMelee()) {
                // A piece standing in a forest can't be hit by a ranged attack, so check this first.
                return Optional.empty();
            }
            if (view.isOccupied(square)) {
                return Optional.of(new Found(square, distance));
            }
        }
        return Optional.empty();
    }

    /**
     * Slides a piece from {@code start} up to {@code maxSquares} in one direction, stopping
     * before anything it can't enter: walls, rivers, other pieces or the board edge.
     *
     * @return where the piece ends up ({@code start} if it can't move at all)
     */
    static Position slide(BoardView view, Position start, Direction direction, int maxSquares) {
        Position current = start;
        for (int i = 0; i < maxSquares; i++) {
            Position next = current.step(direction);
            if (!view.isFreeToEnter(next)) {
                break;
            }
            current = next;
        }
        return current;
    }

    /** Ranged attacks can't be fired from inside a forest. */
    static boolean canFireFrom(BoardView view, Position from) {
        return !view.board().tileAt(from).blocksArrows();
    }
}
