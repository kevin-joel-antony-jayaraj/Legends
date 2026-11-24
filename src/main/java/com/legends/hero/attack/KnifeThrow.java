package com.legends.hero.attack;

import com.legends.board.BoardView;
import com.legends.model.Direction;
import com.legends.model.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * The Pawn's one-per-game Knife throw: hits the first piece in any of the eight
 * directions, up to {@code range} squares away, poisons it, and leaves the Keystone on
 * its square.
 *
 * <p>A throw is a ranged attack, so it's blocked by walls and can't go into a forest
 * (it can be thrown out of one). Whether the Pawn still has its knife is the Pawn's
 * business; this class only works out the targets.
 */
public final class KnifeThrow implements Attack {

    private final String id;
    private final int range;
    private final int halfHearts;
    private final int poisonTurns;
    private final int poisonHalfHeartsPerTurn;

    public KnifeThrow(String id, int range, int halfHearts, int poisonTurns, int poisonHalfHeartsPerTurn) {
        this.id = id;
        this.range = range;
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
        List<AttackOption> options = new ArrayList<>();
        for (Direction direction : Direction.ALL) {
            Lines.firstPiece(view, from, direction, range, true).ifPresent(found -> {
                Position target = found.target();
                options.add(new AttackOption(id, target, from, List.of(new Hit(target, halfHearts)), List.of(
                        new Effect.Poison(target, poisonTurns, poisonHalfHeartsPerTurn),
                        new Effect.DropKeystone(target))));
            });
        }
        return options;
    }
}
