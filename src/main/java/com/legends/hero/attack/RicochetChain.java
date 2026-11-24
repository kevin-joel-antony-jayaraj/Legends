package com.legends.hero.attack;

import com.legends.board.BoardView;
import com.legends.model.Direction;
import com.legends.model.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Hits one adjacent piece, then bounces to every other piece near it.
 * The Engineer-stage Pawn's attack: 3 hearts to the target, 1.5 hearts to each piece
 * within 2 squares of the target. It punishes players who bunch up against the Pawn.
 */
public final class RicochetChain implements Attack {

    private final String id;
    private final int primaryHalfHearts;
    private final int chainHalfHearts;
    private final int chainRadius;

    public RicochetChain(String id, int primaryHalfHearts, int chainHalfHearts, int chainRadius) {
        this.id = id;
        this.primaryHalfHearts = primaryHalfHearts;
        this.chainHalfHearts = chainHalfHearts;
        this.chainRadius = chainRadius;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public List<AttackOption> options(Position from, BoardView view) {
        List<AttackOption> options = new ArrayList<>();
        for (Direction direction : Direction.ALL) {
            Position target = from.step(direction);
            if (!view.board().contains(target) || !view.isOccupied(target)) {
                continue;
            }
            List<Hit> hits = new ArrayList<>();
            hits.add(new Hit(target, primaryHalfHearts));
            for (Position nearby : squaresAround(view, target)) {
                if (!nearby.equals(target) && !nearby.equals(from) && view.isOccupied(nearby)) {
                    hits.add(new Hit(nearby, chainHalfHearts));
                }
            }
            options.add(new AttackOption(id, target, from, hits));
        }
        return options;
    }

    /** Every on-board square within the chain radius of the centre, counting diagonals as 1. */
    private List<Position> squaresAround(BoardView view, Position centre) {
        List<Position> squares = new ArrayList<>();
        for (int dRow = -chainRadius; dRow <= chainRadius; dRow++) {
            for (int dCol = -chainRadius; dCol <= chainRadius; dCol++) {
                Position p = centre.offset(dRow, dCol);
                if (view.board().contains(p)) {
                    squares.add(p);
                }
            }
        }
        return squares;
    }
}
