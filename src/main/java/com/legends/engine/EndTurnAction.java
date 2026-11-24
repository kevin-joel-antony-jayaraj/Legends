package com.legends.engine;

import com.legends.game.GameState;
import java.util.Optional;

/** End the turn early, giving up any actions left. Costs nothing. */
public record EndTurnAction(int seat) implements Action {

    @Override
    public int cost() {
        return 0;
    }

    @Override
    public boolean endsTurn() {
        return true;
    }

    @Override
    public Optional<String> problem(GameState game) {
        return Optional.empty();
    }

    @Override
    public void apply(ActionContext context) {
        // Nothing to do here: the engine hands the turn to the next player.
    }
}
