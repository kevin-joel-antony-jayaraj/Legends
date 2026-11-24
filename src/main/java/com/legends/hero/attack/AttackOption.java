package com.legends.hero.attack;

import com.legends.model.Position;
import java.util.List;

/**
 * One concrete way to use an attack right now: what it aims at, where the attacker
 * ends up, and exactly which squares take how much damage.
 *
 * <p>Options are worked out in full before anything happens. The game shows them to the
 * player, and an attack request is only accepted if it matches one of them.
 *
 * @param attackId which attack this is, for example "diagonal-shot"
 * @param aim      the square the player picks (usually the main target)
 * @param endsAt   where the attacker stands afterwards (its own square if it doesn't move)
 * @param hits     every square hit and the damage to each
 */
public record AttackOption(String attackId, Position aim, Position endsAt, List<Hit> hits) {

    public AttackOption {
        hits = List.copyOf(hits);
        if (hits.isEmpty()) {
            throw new IllegalArgumentException("An attack option must hit something");
        }
    }
}
