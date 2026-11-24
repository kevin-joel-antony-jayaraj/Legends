package com.legends.engine;

import com.legends.engine.event.GameEvent;
import com.legends.game.GameState;
import com.legends.hero.Hero;
import com.legends.model.Position;
import java.util.List;
import java.util.Optional;

/** Move the current hero to a square its movement pattern allows. Costs 1 action. */
public record MoveAction(int seat, Position to) implements Action {

    @Override
    public int cost() {
        return 1;
    }

    @Override
    public Optional<String> problem(GameState game) {
        Hero hero = game.heroInSeat(seat);
        if (!game.legalMoves(hero).contains(to)) {
            return Optional.of(hero.type() + " can't move to " + to);
        }
        return Optional.empty();
    }

    @Override
    public void apply(GameState game, List<GameEvent> events) {
        Hero hero = game.heroInSeat(seat);
        Position from = game.positionOf(hero).orElseThrow();
        game.moveHero(hero, to);
        events.add(new GameEvent.HeroMoved(seat, from, to));
    }
}
