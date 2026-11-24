package com.legends.hero.attack;

import com.legends.board.BoardView;
import com.legends.model.Direction;
import com.legends.model.Position;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The Engineer-stage Pawn's attack: a shot that ricochets from enemy to enemy and doubles
 * its damage on every bounce.
 *
 * <ol>
 *   <li>The shot flies along one of the 8 lines, up to {@code range} squares, and hits the
 *       first piece. Like an arrow, it's blocked by walls and can't go into a forest
 *       (it can be fired out of one).</li>
 *   <li>It then bounces to the nearest piece within {@code bounceRadius} squares of the
 *       last one it hit, skipping pieces it already hit and the Pawn itself. Bounces are
 *       ricochets, not line shots, so they jump over walls and forest.</li>
 *   <li>Each hit does twice the one before (2, 4, 8 hearts). It stops after
 *       {@code maxHits} hits, or when nobody new is in reach.</li>
 * </ol>
 *
 * <p>When two pieces are equally near, the bounce goes to the one that comes first reading
 * the board top to bottom, left to right. That keeps the result predictable, so the
 * preview a player sees is exactly what happens.
 */
public final class RicochetChain implements Attack {

    private final String id;
    private final int range;
    private final int firstHalfHearts;
    private final int bounceRadius;
    private final int maxHits;

    /**
     * @param range           how far the first shot flies
     * @param firstHalfHearts damage of the first hit; every bounce doubles it
     * @param bounceRadius    how far a bounce can reach from the last piece hit
     * @param maxHits         most pieces hit in one chain, counting the first
     */
    public RicochetChain(String id, int range, int firstHalfHearts, int bounceRadius, int maxHits) {
        this.id = id;
        this.range = range;
        this.firstHalfHearts = firstHalfHearts;
        this.bounceRadius = bounceRadius;
        this.maxHits = maxHits;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public List<AttackOption> options(Position from, BoardView view) {
        List<AttackOption> options = new ArrayList<>();
        for (Direction direction : Direction.ALL) {
            Lines.firstPiece(view, from, direction, range, true)
                    .ifPresent(found -> options.add(
                            new AttackOption(id, found.target(), from, chain(view, from, found.target()))));
        }
        return options;
    }

    /** The hits of one shot: the first target, then each bounce at double the damage. */
    private List<Hit> chain(BoardView view, Position from, Position firstTarget) {
        List<Hit> hits = new ArrayList<>();
        Optional<Position> current = Optional.of(firstTarget);
        int damage = firstHalfHearts;
        while (current.isPresent() && hits.size() < maxHits) {
            hits.add(new Hit(current.get(), damage));
            damage *= 2;
            current = nextBounce(view, from, current.get(), hits);
        }
        return hits;
    }

    /** The nearest piece within reach of {@code last} that hasn't been hit yet and isn't the shooter. */
    private Optional<Position> nextBounce(BoardView view, Position from, Position last, List<Hit> alreadyHit) {
        List<Position> candidates = new ArrayList<>();
        for (int dRow = -bounceRadius; dRow <= bounceRadius; dRow++) {
            for (int dCol = -bounceRadius; dCol <= bounceRadius; dCol++) {
                Position square = last.offset(dRow, dCol);
                boolean hitBefore = alreadyHit.stream().anyMatch(hit -> hit.target().equals(square));
                if (view.board().contains(square) && view.isOccupied(square) && !square.equals(from) && !hitBefore) {
                    candidates.add(square);
                }
            }
        }
        return candidates.stream().min(Comparator.comparingInt((Position p) -> p.kingDistance(last))
                .thenComparingInt(Position::row)
                .thenComparingInt(Position::col));
    }
}
