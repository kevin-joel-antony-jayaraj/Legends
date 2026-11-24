package com.legends.cli;

import com.legends.engine.event.GameEvent;
import com.legends.game.GameState;
import com.legends.hero.Hearts;
import com.legends.hero.Hero;
import com.legends.model.Position;

/**
 * Turns game events into sentences for the console.
 *
 * <p>Because {@link GameEvent} is sealed, the switch below must handle every kind of
 * event. Add a new event and forget it here, and the code won't compile.
 */
final class EventText {

    private EventText() {
    }

    static String describe(GameEvent event, GameState game) {
        return switch (event) {
            case GameEvent.HeroMoved e ->
                    name(game, e.seat()) + " moves " + square(e.from()) + " -> " + square(e.to());
            case GameEvent.HeroDamaged e ->
                    name(game, e.targetSeat()) + " takes " + Hearts.format(e.halfHearts()) + " hearts from "
                            + name(game, e.attackerSeat()) + "'s " + e.attackId()
                            + " (" + Hearts.format(e.halfHeartsLeft()) + " left)";
            case GameEvent.HeroDefeated e -> name(game, e.seat()) + " is defeated!";
            case GameEvent.PawnEvolved e -> name(game, e.seat()) + " evolves into " + e.stage() + "!";
            case GameEvent.TurnStarted e ->
                    "--- " + name(game, e.seat()) + "'s turn (" + e.actions() + " actions) ---";
            case GameEvent.TurnSkipped e -> name(game, e.seat()) + " has no legal action and is skipped";
            case GameEvent.GameOver e -> e.winnerSeat() == GameEvent.GameOver.NO_WINNER
                    ? "Game over: nobody survived."
                    : "Game over: " + name(game, e.winnerSeat()) + " wins!";
        };
    }

    static String name(GameState game, int seat) {
        Hero hero = game.heroInSeat(seat);
        return hero.owner().name() + " (" + hero.type() + ")";
    }

    static String square(Position p) {
        return p.toString();
    }
}
