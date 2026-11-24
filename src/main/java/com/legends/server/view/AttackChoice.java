package com.legends.server.view;

import com.legends.hero.attack.Hit;
import com.legends.model.Position;
import java.util.List;

/**
 * One attack the player could make right now, with everything needed to show it and to
 * send it back: the client posts {@code attackId}, {@code aim}, {@code landing} and
 * {@code pushTo} unchanged to choose it.
 *
 * @param landing where the attacker ends up
 * @param pushTo  where the first pushed piece ends up, or null if nothing is pushed
 */
public record AttackChoice(String attackId, boolean isSuper, Position aim, Position landing, Position pushTo,
                           List<Hit> hits) {
}
