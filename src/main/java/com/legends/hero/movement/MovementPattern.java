package com.legends.hero.movement;

import com.legends.board.BoardView;
import com.legends.model.Position;
import java.util.Set;

/**
 * How a hero moves, as a rule that lists the squares it could move to.
 *
 * <p>This is the "strategy" pattern: each hero has a movement pattern object rather
 * than its own copy of movement code. The Archer and Pawn share {@link KingStep}, and
 * a future Gamemaster piece could reuse any of these.
 */
public interface MovementPattern {

    /** Every square a hero standing on {@code from} could move to with one move action. */
    Set<Position> destinations(Position from, BoardView view);
}
