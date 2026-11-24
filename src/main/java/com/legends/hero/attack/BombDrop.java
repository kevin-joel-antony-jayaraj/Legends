package com.legends.hero.attack;

import com.legends.board.BoardView;
import com.legends.model.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * The Knight's Super: hides a bomb on any empty square on the board.
 *
 * <p>The options are every walkable, unoccupied square. Attacks can't see bombs, so a
 * square that already has one is listed too; only one bomb fits on a square, so the
 * engine refuses it when it's chosen (see {@code AttackAction}). That way the list never
 * shows where the bombs are, not even to the Knight.
 */
public final class BombDrop implements Attack {

    private final String id;

    public BombDrop(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public List<AttackOption> options(Position from, BoardView view) {
        List<AttackOption> options = new ArrayList<>();
        for (int row = 0; row < view.board().rows(); row++) {
            for (int col = 0; col < view.board().cols(); col++) {
                Position square = new Position(row, col);
                if (view.isFreeToEnter(square)) {
                    options.add(new AttackOption(id, square, from, List.of(), List.of(new Effect.PlaceBomb(square))));
                }
            }
        }
        return options;
    }
}
