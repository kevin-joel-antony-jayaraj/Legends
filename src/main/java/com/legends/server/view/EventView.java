package com.legends.server.view;

import com.legends.engine.event.GameEvent;

/**
 * A game event ready to send as JSON: its kind as a string (like "HeroDamaged") plus its
 * fields. Events never carry secrets (a placed bomb's event has no position), so the same
 * list can go to every player.
 */
public record EventView(String type, GameEvent data) {

    public static EventView of(GameEvent event) {
        return new EventView(event.getClass().getSimpleName(), event);
    }
}
