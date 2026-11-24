package com.legends.engine;

import com.legends.engine.event.GameEvent;
import com.legends.game.GameState;
import com.legends.hero.Hero;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The referee. Every change to a game goes through {@link #perform}, which checks the
 * request, applies it, and reports what happened as a list of events.
 *
 * <p>This is the class the server will call later: a player's request comes in as an
 * {@link Action}, and the events go back out to every player.
 */
public final class GameEngine {

    private final GameState game;

    public GameEngine(GameState game) {
        this.game = game;
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

        List<GameEvent> events = new ArrayList<>();
        action.apply(game, events);
        for (int i = 0; i < action.cost(); i++) {
            game.spendAction();
        }

        for (Hero defeated : game.removeDeadHeroes()) {
            events.add(new GameEvent.HeroDefeated(defeated.owner().seat()));
        }
        if (game.isOver()) {
            int winner = game.winner().map(h -> h.owner().seat()).orElse(GameEvent.GameOver.NO_WINNER);
            events.add(new GameEvent.GameOver(winner));
            return events;
        }

        if (action.endsTurn() || game.actionsLeft() == 0) {
            passTurn(events);
        }
        return events;
    }

    /**
     * Hands the turn clockwise to the next hero that can do something. A hero with no
     * legal move or attack is skipped (docs/RULES.md). If every hero is stuck, the turn
     * stops after one full lap instead of looping forever.
     */
    private void passTurn(List<GameEvent> events) {
        int seats = game.heroes().size();
        for (int tried = 0; tried < seats; tried++) {
            game.endTurn();
            Hero next = game.currentHero();
            if (game.hasAnyLegalAction(next)) {
                events.add(new GameEvent.TurnStarted(next.owner().seat(), game.actionsLeft()));
                return;
            }
            events.add(new GameEvent.TurnSkipped(next.owner().seat()));
        }
    }
}
