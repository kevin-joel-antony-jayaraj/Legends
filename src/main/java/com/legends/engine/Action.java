package com.legends.engine;

import com.legends.game.GameState;
import java.util.Optional;

/**
 * Something a player asks to do on their turn. This is the "command" pattern: a request
 * is an object that can be checked, applied, logged, or sent over a network.
 *
 * <p>Actions never run on their own. The {@link GameEngine} first checks the rules every
 * action shares (is the game over, is it your turn, do you have actions left), then asks
 * the action for its own rule check, and only then applies it.
 *
 * <p>Sealed: these records are the only actions, which keeps the set of things a
 * client can request small and known.
 */
public sealed interface Action permits MoveAction, AttackAction, EndTurnAction {

    /** The seat of the player making the request. */
    int seat();

    /** How many of the hero's actions this uses up. */
    int cost();

    /** True if this action ends the turn straight away. */
    default boolean endsTurn() {
        return false;
    }

    /** Why this action isn't allowed right now, or empty if it is. Changes nothing. */
    Optional<String> problem(GameState game);

    /** Carries out the action. Only called after {@link #problem} found nothing. */
    void apply(ActionContext context);
}
