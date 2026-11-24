package com.legends.hero.attack;

import com.legends.model.Position;
import java.util.List;
import java.util.Optional;

/**
 * One concrete way to use an attack right now: what it aims at, where the attacker
 * ends up, which squares take how much damage, and any other effects.
 *
 * <p>Options are worked out in full before anything happens. The game shows them to the
 * player, and an attack request is only accepted if it matches one of them.
 *
 * @param attackId which attack this is, for example "diagonal-shot"
 * @param aim      the square the player picks (usually the main target)
 * @param endsAt   where the attacker stands afterwards (its own square if it doesn't move)
 * @param hits     every square hit and the damage to each (may be empty, like the Castle's Pull)
 * @param effects  everything else the attack does: pushes, poison, Keystone, bombs
 */
public record AttackOption(String attackId, Position aim, Position endsAt, List<Hit> hits, List<Effect> effects) {

    public AttackOption {
        hits = List.copyOf(hits);
        effects = List.copyOf(effects);
        if (hits.isEmpty() && effects.isEmpty()) {
            throw new IllegalArgumentException("An attack option must hit something or have an effect");
        }
    }

    /** An option that only deals damage. */
    public AttackOption(String attackId, Position aim, Position endsAt, List<Hit> hits) {
        this(attackId, aim, endsAt, hits, List.of());
    }

    /** Where the first piece pushed by this option ends up, if it pushes anything. */
    public Optional<Position> firstPushDestination() {
        return effects.stream()
                .filter(Effect.Push.class::isInstance)
                .map(e -> ((Effect.Push) e).to())
                .findFirst();
    }
}
