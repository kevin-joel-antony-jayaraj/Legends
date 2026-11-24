package com.legends.hero.attack;

import com.legends.board.BoardView;
import com.legends.model.Direction;
import com.legends.model.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * The Pawn's one-per-game Knife throw: hits the first piece in any of the eight
 * directions at any range, poisons it, and leaves the Keystone on its square.
 *
 * <p>A throw is a ranged attack, so it's blocked by forest and walls and can't be made
 * from inside a forest. Whether the Pawn still has its knife is the Pawn's business; this
 * class only works out the targets.
 */
public final class KnifeThrow implements Attack {

    private final String id;
    private final int halfHearts;
    private final int poisonTurns;
    private final int poisonHalfHeartsPerTurn;

    public KnifeThrow(String id, int halfHearts, int poisonTurns, int poisonHalfHeartsPerTurn) {
        this.id = id;
        this.halfHearts = halfHearts;
        this.poisonTurns = poisonTurns;
        this.poisonHalfHeartsPerTurn = poisonHalfHeartsPerTurn;
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
            Lines.firstPiece(view, from, direction, Integer.MAX_VALUE, true).ifPresent(found -> {
                Position target = found.target();
                options.add(new AttackOption(id, target, from, List.of(new Hit(target, halfHearts)), List.of(
                        new Effect.Poison(target, poisonTurns, poisonHalfHeartsPerTurn),
                        new Effect.DropKeystone(target))));
            });
        }
        return options;
    }
}
