package com.legends.hero.attack;

import com.legends.board.BoardView;
import com.legends.model.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * The Knight's Super: hides a bomb on any empty square on the board.
 *
 * <p>The options are every walkable, unoccupied square without a bomb already on it. Only
 * one bomb fits on a square. The Knight's player can work out where their bombs are from
 * this list; per the rules, keeping that secret is their job.
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
                if (view.isFreeToEnter(square) && !view.hasBomb(square)) {
                    options.add(new AttackOption(id, square, from, List.of(), List.of(new Effect.PlaceBomb(square))));
                }
            }
        }
        return options;
    }
}
