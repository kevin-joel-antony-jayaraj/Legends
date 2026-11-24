package com.legends.hero.attack;

import com.legends.board.BoardView;
import com.legends.model.Direction;
import com.legends.model.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * The Archer's Super: hits the first piece in any of the eight directions and pushes it
 * straight away from the Archer, up to a set number of squares.
 *
 * <p>Aims like a ranged shot (blocked by forest and walls, can't be fired from a forest).
 * The push stops early at a wall, another piece, the board edge or the river's edge.
 */
public final class PushBack implements Attack {

    private final String id;
    private final int range;
    private final int pushSquares;
    private final int halfHearts;

    public PushBack(String id, int range, int pushSquares, int halfHearts) {
        this.id = id;
        this.range = range;
        this.pushSquares = pushSquares;
        this.halfHearts = halfHearts;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public List<AttackOption> options(Position from, BoardView view) {
        if (!Lines.canFireFrom(view, from)) {
            return List.of();
        }
        List<AttackOption> options = new ArrayList<>();
        for (Direction direction : Direction.ALL) {
            Lines.firstPiece(view, from, direction, range, true).ifPresent(found -> {
                Position target = found.target();
                Position pushedTo = Lines.slide(view, target, direction, pushSquares);
                List<Effect> effects = pushedTo.equals(target) ? List.of() : List.of(new Effect.Push(target, pushedTo));
                options.add(new AttackOption(id, target, from, List.of(new Hit(target, halfHearts)), effects));
            });
        }
        return options;
    }
}
