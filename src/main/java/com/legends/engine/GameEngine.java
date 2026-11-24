package com.legends.engine;

import com.legends.engine.event.GameEvent;
import com.legends.game.GameState;
import com.legends.hero.Hero;
import com.legends.model.Seeds;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * The referee. Every change to a game goes through {@link #perform}, which checks the
 * request, applies it, and reports what happened as a list of events.
 *
 * <p>This is the class the server will call later: a player's request comes in as an
 * {@link Action}, and the events go back out to every player.
 */
public final class GameEngine {

    /** Keeps the engine's random numbers (Keystone teleports) separate from board and setup. */
    private static final long ENGINE_SALT = 0x5EED_E461_0000_0003L;

    private final GameState game;
    private final Random random;

    public GameEngine(GameState game) {
        this.game = game;
        this.random = Seeds.random(game.seed(), ENGINE_SALT);
    }

    /** The game being run. Read from it freely; change it only through {@link #perform}. */
    public GameState game() {
        return game;
    }

    /** Why an action isn't allowed right now, or empty if it is. Changes nothing. */
    public Optional<String> check(Action action) {
        if (game.isOver()) {
            return Optional.of("The game is over");
        }
        if (action.seat() != game.currentSeat()) {
            return Optional.of("It's not your turn");
        }
        if (action.cost() > game.actionsLeft()) {
            return Optional.of("No actions left this turn");
        }
        return action.problem(game);
    }

    /**
     * Checks and carries out an action.
     *
     * @return what happened, in order
     * @throws IllegalActionException if the action isn't allowed (the game is unchanged)
     */
    public List<GameEvent> perform(Action action) {
        Optional<String> problem = check(action);
        if (problem.isPresent()) {
            throw new IllegalActionException(problem.get());
        }

        ActionContext context = new ActionContext(game, random);
        action.apply(context);
        for (int i = 0; i < action.cost(); i++) {
            game.spendAction();
        }

        if (removeDefeatedAndCheckGameOver(context)) {
            return context.events();
        }
        if (action.endsTurn() || game.actionsLeft() == 0 || !game.currentHero().isAlive()) {
            passTurn(context);
        }
        return context.events();
    }

    /**
     * Hands the turn clockwise to the next hero that can do something. Poison ticks at the
     * start of each hero's turn, and may defeat it. A hero with no legal move or attack is
     * skipped (docs/RULES.md). If every hero is stuck, the turn stops after one full lap
     * instead of looping forever.
     */
    private void passTurn(ActionContext context) {
        int seats = game.heroes().size();
        for (int tried = 0; tried < seats; tried++) {
            game.endTurn();
            Hero next = game.currentHero();

            tickPoison(context, next);
            if (removeDefeatedAndCheckGameOver(context)) {
                return;
            }
            if (!next.isAlive()) {
                continue;
            }

            if (game.hasAnyLegalAction(next)) {
                context.report(new GameEvent.TurnStarted(next.owner().seat(), game.actionsLeft()));
                return;
            }
            context.report(new GameEvent.TurnSkipped(next.owner().seat()));
        }
    }

    private static void tickPoison(ActionContext context, Hero hero) {
        hero.tickPoison().ifPresent(poison -> {
            int lost = hero.takeDamage(poison.halfHeartsPerTurn());
            if (lost > 0) {
                context.report(new GameEvent.HeroDamaged(hero.owner().seat(), poison.sourceSeat(), "poison",
                        lost, hero.halfHearts()));
            }
        });
    }

    /** @return true if the game just ended */
    private boolean removeDefeatedAndCheckGameOver(ActionContext context) {
        for (Hero defeated : game.removeDeadHeroes()) {
            context.report(new GameEvent.HeroDefeated(defeated.owner().seat()));
        }
        if (game.isOver()) {
            int winner = game.winner().map(h -> h.owner().seat()).orElse(GameEvent.GameOver.NO_WINNER);
            context.report(new GameEvent.GameOver(winner));
            return true;
        }
        return false;
    }
}
