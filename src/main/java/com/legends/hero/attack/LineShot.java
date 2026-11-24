package com.legends.hero.attack;

import com.legends.board.BoardView;
import com.legends.model.Direction;
import com.legends.model.Position;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * Hits the first piece along a straight line, in any of a set of directions.
 *
 * <p>One class covers several heroes' attacks, configured differently:
 * <ul>
 *   <li>Archer's diagonal shot: ranged, diagonals, unlimited range, 2 hearts.</li>
 *   <li>Archer's melee: orthogonals, 1 square, 2 hearts.</li>
 *   <li>Castle's lane shot: ranged, orthogonals, unlimited, damage falls off with distance.</li>
 *   <li>Pawn's Knife: all directions, 2 squares, damage falls off with distance.</li>
 * </ul>
 */
public final class LineShot implements Attack {

    /** Use as max range for "unlimited": the line always stops at the board edge first. */
    public static final int UNLIMITED = Integer.MAX_VALUE;

    private final String id;
    private final List<Direction> directions;
    private final int maxRange;
    private final boolean ranged;
    private final IntUnaryOperator damageAtDistance;

    private LineShot(String id, List<Direction> directions, int maxRange, boolean ranged,
                     IntUnaryOperator damageAtDistance) {
        if (maxRange <= 0) {
            throw new IllegalArgumentException("Range must be positive, got " + maxRange);
        }
        this.id = id;
        this.directions = List.copyOf(directions);
        this.maxRange = maxRange;
        this.ranged = ranged;
        this.damageAtDistance = damageAtDistance;
    }

    /**
     * A ranged attack: blocked by forest and walls, and can't be fired from a forest.
     *
     * @param damageAtDistance half-hearts of damage for a target this many squares away
     */
    public static LineShot ranged(String id, List<Direction> directions, int maxRange, IntUnaryOperator damageAtDistance) {
        return new LineShot(id, directions, maxRange, true, damageAtDistance);
    }

    /** A melee attack with reach: blocked only by walls, and works in and into forests. */
    public static LineShot melee(String id, List<Direction> directions, int maxRange, IntUnaryOperator damageAtDistance) {
        return new LineShot(id, directions, maxRange, false, damageAtDistance);
    }

    @Override
    public String id() {
        return id;
    }

    public boolean isRanged() {
        return ranged;
    }

    @Override
    public List<AttackOption> options(Position from, BoardView view) {
        if (ranged && !Lines.canFireFrom(view, from)) {
            return List.of();
        }
        List<AttackOption> options = new ArrayList<>();
        for (Direction direction : directions) {
            Lines.firstPiece(view, from, direction, maxRange, ranged).ifPresent(found -> {
                int damage = damageAtDistance.applyAsInt(found.distance());
                if (damage > 0) {
                    options.add(new AttackOption(id, found.target(), from, List.of(new Hit(found.target(), damage))));
                }
            });
        }
        return options;
    }
}
