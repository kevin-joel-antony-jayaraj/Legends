package com.legends.hero.attack;

import com.legends.board.BoardView;
import com.legends.model.Position;
import java.util.List;

/**
 * An attack a hero can make, as a rule that lists every way it could be used.
 *
 * <p>Attacks are pure calculations: {@link #options} looks at the board and returns
 * what would happen, but never changes anything. Applying the damage is the game
 * engine's job. That split makes every attack easy to test on its own, and lets a UI
 * or AI ask "what can I do?" using exactly the same code that enforces the rules.
 *
 * <p>Attacks only see squares and occupancy, not heroes. In free-for-all every other
 * piece is an enemy, so "is there a piece here?" is all an attack needs to know.
 */
public interface Attack {

    /** A short, stable name, for example "diagonal-shot". Used by players to pick an attack. */
    String id();

    /** Every way this attack could be used by a hero standing on {@code from}. Empty if none. */
    List<AttackOption> options(Position from, BoardView view);
}
