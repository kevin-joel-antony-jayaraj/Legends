package com.legends.hero.attack;

import com.legends.board.BoardView;
import com.legends.model.Direction;
import com.legends.model.Position;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Knight's "Check": a jump along the L that lands on an enemy and knocks it back.
 *
 * <p>An L has three stop points: early (1 square out), middle (the bend), and the end.
 * The Knight picks an enemy on one of them. The enemy takes damage by stop point
 * (early 1, middle 2, end 3 hearts) and is knocked back one square in the direction the
 * Knight is facing, which is the direction of its last step into that square. The Knight
 * takes the enemy's square. Hitting the end of the L also splashes every other piece next
 * to the target for 3 hearts.
 *
 * <p>If the knockback square is blocked (wall, river, board edge or another piece), the
 * enemy stays put and the Knight lands on the square just before it along the L instead
 * (or stays where it is, for an early stop).
 *
 * <p>Each L can be walked two ways (2-then-1 or 1-then-2), each with its own facing at
 * the end, so the player can choose which way to knock the target. The Knight jumps
 * over anything on the way, but must land on a walkable square.
 */
public final class KnightCheck implements Attack {

    /** A square on an L path, and the direction the Knight faces when it arrives there. */
    record Step(Position square, Direction facing) {
    }

    private final String id;
    private final int[] halfHeartsByStop;
    private final int splashHalfHearts;

    /**
     * @param halfHeartsByStop damage for the early, middle and end stop points, in that order
     * @param splashHalfHearts damage to each piece next to the target when hitting the end of the L
     */
    public KnightCheck(String id, int[] halfHeartsByStop, int splashHalfHearts) {
        if (halfHeartsByStop.length != 3) {
            throw new IllegalArgumentException("Need damage for exactly 3 stop points");
        }
        this.id = id;
        this.halfHeartsByStop = halfHeartsByStop.clone();
        this.splashHalfHearts = splashHalfHearts;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public List<AttackOption> options(Position from, BoardView view) {
        // Keyed by (aim, landing, push destination) so an option reached by two paths is listed once.
        Map<List<Position>, AttackOption> options = new LinkedHashMap<>();
        for (List<Step> path : allPaths(from)) {
            for (int stop = 0; stop < path.size(); stop++) {
                Position target = path.get(stop).square();
                if (!view.board().contains(target) || !view.isOccupied(target)) {
                    continue;
                }
                List<Hit> hits = new ArrayList<>();
                hits.add(new Hit(target, halfHeartsByStop[stop]));
                if (stop == path.size() - 1) {
                    addSplash(view, from, target, hits);
                }

                Position knockTo = target.step(path.get(stop).facing());
                if (view.isFreeToEnter(knockTo)) {
                    options.putIfAbsent(List.of(target, target, knockTo),
                            new AttackOption(id, target, target, hits, List.of(new Effect.Push(target, knockTo))));
                    continue;
                }

                // Knockback blocked: the target stays, and the Knight stops just short of it.
                Position landing = stop == 0 ? from : path.get(stop - 1).square();
                if (landing.equals(from) || view.isFreeToEnter(landing)) {
                    options.putIfAbsent(List.of(target, landing), new AttackOption(id, target, landing, hits));
                }
            }
        }
        return List.copyOf(options.values());
    }

    private void addSplash(BoardView view, Position from, Position target, List<Hit> hits) {
        for (Direction direction : Direction.ALL) {
            Position next = target.step(direction);
            if (view.board().contains(next) && !next.equals(from) && view.isOccupied(next)) {
                hits.add(new Hit(next, splashHalfHearts));
            }
        }
    }

    /**
     * All 16 ways to walk an L: for each of 4 directions and 2 sides, either
     * "2 then 1" or "1 then 2". Each path lists its early, middle and end steps.
     */
    static List<List<Step>> allPaths(Position from) {
        List<List<Step>> paths = new ArrayList<>();
        for (Direction forward : Direction.ORTHOGONALS) {
            for (Direction side : forward.perpendiculars()) {
                Position one = from.step(forward);
                Position two = one.step(forward);
                paths.add(List.of(new Step(one, forward), new Step(two, forward), new Step(two.step(side), side)));

                Position sideways = from.step(side);
                Position bend = sideways.step(forward);
                paths.add(List.of(new Step(sideways, side), new Step(bend, forward),
                        new Step(bend.step(forward), forward)));
            }
        }
        return paths;
    }
}
