package com.legends.hero.attack;

import com.legends.board.BoardView;
import com.legends.model.Direction;
import com.legends.model.Position;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Knight's "Check": a jump along the L that hits whoever it lands on.
 *
 * <p>An L has three stop points: early (1 square out), middle (the bend), and the end.
 * The Knight picks an enemy on one of those stop points. The enemy takes damage by stop
 * point (early 1, middle 2, end 3 hearts), and the Knight lands on the square just
 * before it along the L (or stays put for an early stop). Hitting the end of the L also
 * splashes every other piece next to the target for 3 hearts.
 *
 * <p>Each L can be walked two ways (2-then-1 or 1-then-2), and the Knight may use
 * either. It jumps over anything on the way, but must land on a free, walkable square.
 *
 * <p>Interpretation of docs/HEROES.md, where a Knight can't share a square with its
 * target: see the open question there.
 */
public final class KnightCheck implements Attack {

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
        // Keyed by (aim, landing) so the same attack reached by two paths is listed once.
        Map<List<Position>, AttackOption> options = new LinkedHashMap<>();
        for (List<Position> path : allPaths(from)) {
            for (int stop = 0; stop < path.size(); stop++) {
                Position target = path.get(stop);
                if (!view.board().contains(target) || !view.isOccupied(target)) {
                    continue;
                }
                Position landing = stop == 0 ? from : path.get(stop - 1);
                if (!landing.equals(from) && !view.isFreeToEnter(landing)) {
                    continue;
                }
                List<Hit> hits = new ArrayList<>();
                hits.add(new Hit(target, halfHeartsByStop[stop]));
                if (stop == path.size() - 1) {
                    addSplash(view, from, target, hits);
                }
                options.putIfAbsent(List.of(target, landing), new AttackOption(id, target, landing, hits));
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
     * "2 then 1" or "1 then 2". Each path lists its early, middle and end squares.
     */
    static List<List<Position>> allPaths(Position from) {
        List<List<Position>> paths = new ArrayList<>();
        for (Direction forward : Direction.ORTHOGONALS) {
            for (Direction side : forward.perpendiculars()) {
                Position one = from.step(forward);
                Position two = one.step(forward);
                paths.add(List.of(one, two, two.step(side)));

                Position sideways = from.step(side);
                Position bend = sideways.step(forward);
                paths.add(List.of(sideways, bend, bend.step(forward)));
            }
        }
        return paths;
    }
}
