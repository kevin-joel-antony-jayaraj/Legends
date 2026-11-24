package com.legends.hero.attack;

import com.legends.board.BoardView;
import com.legends.model.Direction;
import com.legends.model.Position;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Archer's Super: a shockwave. Every piece inside a solid square around the Archer
 * takes damage and is pushed straight out of the square, away from the Archer.
 *
 * <p>"Straight away" means one step at a time in the direction from the Archer to the
 * piece (a diagonal if it isn't in line). A push stops early at a wall, the river's
 * edge, another piece or the board edge. It's an area effect, so it reaches pieces in
 * forests and behind walls.
 *
 * <p>There's nothing to choose, so it has one option, aimed at the Archer's own square.
 */
public final class PushBack implements Attack {

    private final String id;
    private final int radius;
    private final int halfHearts;

    /**
     * @param radius     size of the square around the Archer: 2 means everything within 2 squares (5×5)
     * @param halfHearts damage to each piece caught in it
     */
    public PushBack(String id, int radius, int halfHearts) {
        this.id = id;
        this.radius = radius;
        this.halfHearts = halfHearts;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public List<AttackOption> options(Position from, BoardView view) {
        List<Position> caught = new ArrayList<>();
        for (int dRow = -radius; dRow <= radius; dRow++) {
            for (int dCol = -radius; dCol <= radius; dCol++) {
                Position square = from.offset(dRow, dCol);
                if (!square.equals(from) && view.board().contains(square) && view.isOccupied(square)) {
                    caught.add(square);
                }
            }
        }
        if (caught.isEmpty()) {
            return List.of();
        }

        // Push the farthest pieces first, so they make room for the ones behind them.
        caught.sort(Comparator.comparingInt((Position p) -> p.kingDistance(from)).reversed()
                .thenComparingInt(Position::row).thenComparingInt(Position::col));

        Set<Position> vacated = new HashSet<>();
        Set<Position> filled = new HashSet<>();
        List<Hit> hits = new ArrayList<>();
        List<Effect> pushes = new ArrayList<>();
        for (Position target : caught) {
            hits.add(new Hit(target, halfHearts));
            Direction away = awayFrom(from, target);
            Position current = target;
            while (current.kingDistance(from) <= radius) {
                Position next = current.step(away);
                boolean occupied = filled.contains(next) || (view.isOccupied(next) && !vacated.contains(next));
                if (!view.board().contains(next) || !view.board().tileAt(next).isWalkable() || occupied) {
                    break;
                }
                current = next;
            }
            if (!current.equals(target)) {
                vacated.add(target);
                filled.add(current);
                pushes.add(new Effect.Push(target, current));
            }
        }
        return List.of(new AttackOption(id, from, from, hits, pushes));
    }

    /** The one-step direction from {@code from} toward {@code target}, extended away from {@code from}. */
    private static Direction awayFrom(Position from, Position target) {
        int dRow = Integer.signum(target.row() - from.row());
        int dCol = Integer.signum(target.col() - from.col());
        for (Direction direction : Direction.ALL) {
            if (direction.dRow() == dRow && direction.dCol() == dCol) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Target is on the Archer's own square");
    }
}
