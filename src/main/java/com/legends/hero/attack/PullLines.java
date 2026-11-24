package com.legends.hero.attack;

import com.legends.board.BoardView;
import com.legends.model.Direction;
import com.legends.model.Position;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Castle's Super: pulls the first piece on each of the four straight lines toward
 * the Castle, ending next to it. No damage.
 *
 * <p>A wall between the Castle and a piece blocks the pull on that line. A pulled piece
 * stops early at a river's edge (it can't be pulled across), another piece, or a wall.
 * Forests don't block a pull. There's nothing to choose, so there is one option, aimed at
 * the Castle's own square, and only if at least one piece would actually move.
 */
public final class PullLines implements Attack {

    private final String id;

    public PullLines(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public List<AttackOption> options(Position from, BoardView view) {
        List<Effect> pulls = new ArrayList<>();
        for (Direction direction : Direction.ORTHOGONALS) {
            firstPieceBeforeAWall(view, from, direction).ifPresent(found -> {
                // Never further than the square next to the Castle.
                int maxSquares = found.distance() - 1;
                Position pulledTo = Lines.slide(view, found.target(), direction.opposite(), maxSquares);
                if (!pulledTo.equals(found.target())) {
                    pulls.add(new Effect.Push(found.target(), pulledTo));
                }
            });
        }
        return pulls.isEmpty() ? List.of() : List.of(new AttackOption(id, from, from, List.of(), pulls));
    }

    /** The first piece along the line, looking over rivers and forest but not past a wall. */
    private static Optional<Lines.Found> firstPieceBeforeAWall(BoardView view, Position from, Direction direction) {
        Position square = from.step(direction);
        for (int distance = 1; view.board().contains(square); distance++) {
            if (view.board().tileAt(square).blocksMelee()) {
                return Optional.empty();
            }
            if (view.isOccupied(square)) {
                return Optional.of(new Lines.Found(square, distance));
            }
            square = square.step(direction);
        }
        return Optional.empty();
    }
}
