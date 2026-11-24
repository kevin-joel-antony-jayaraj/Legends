package com.legends.hero.attack;

import com.legends.board.BoardView;
import com.legends.model.Direction;
import com.legends.model.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Fires along all eight lines at once and hits the first piece on each.
 * The Queen-stage Pawn's attack: ranged, 4 squares, 2 hearts per line. Like every
 * ranged attack, it can be fired out of a forest but not into one.
 *
 * <p>There's nothing to choose, so it has a single option aimed at the attacker's own square.
 */
public final class QueenLines implements Attack {

    private final String id;
    private final int maxRange;
    private final int halfHeartsPerLine;

    public QueenLines(String id, int maxRange, int halfHeartsPerLine) {
        this.id = id;
        this.maxRange = maxRange;
        this.halfHeartsPerLine = halfHeartsPerLine;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public List<AttackOption> options(Position from, BoardView view) {
        List<Hit> hits = new ArrayList<>();
        for (Direction direction : Direction.ALL) {
            Lines.firstPiece(view, from, direction, maxRange, true)
                    .ifPresent(found -> hits.add(new Hit(found.target(), halfHeartsPerLine)));
        }
        return hits.isEmpty() ? List.of() : List.of(new AttackOption(id, from, from, hits));
    }
}
