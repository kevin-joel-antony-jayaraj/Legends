package com.legends.engine;

import com.legends.engine.event.GameEvent;
import com.legends.game.GameState;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * What an action works with while it is applied: the game, the game's random number
 * generator (for things like the Keystone teleport), and the list of events to report.
 *
 * <p>Bundling these means adding something new later (a log, a replay recorder) changes
 * this class only, not the signature of every action.
 */
public final class ActionContext {

    private final GameState game;
    private final Random random;
    private final List<GameEvent> events = new ArrayList<>();

    ActionContext(GameState game, Random random) {
        this.game = game;
        this.random = random;
    }

    public GameState game() {
        return game;
    }

    Random random() {
        return random;
    }

    public void report(GameEvent event) {
        events.add(event);
    }

    List<GameEvent> events() {
        return events;
    }
}
