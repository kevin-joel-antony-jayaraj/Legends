package com.legends.hero.attack;

import com.legends.model.Position;

/**
 * Something an attack does besides damage. Like hits, effects are only descriptions:
 * the game engine carries them out.
 *
 * <p>Sealed, so the engine's {@code switch} over effects is checked by the compiler to
 * handle every kind.
 */
public sealed interface Effect {

    /** Moves the piece standing on {@code from} to {@code to} (knockback, push, pull). */
    record Push(Position from, Position to) implements Effect {
    }

    /**
     * Poisons the piece on {@code target}: it loses {@code halfHeartsPerTurn} at the
     * start of each of its next {@code turns} turns.
     */
    record Poison(Position target, int turns, int halfHeartsPerTurn) implements Effect {
    }

    /** Leaves the attacker's Keystone on a square. */
    record DropKeystone(Position at) implements Effect {
    }

    /** Hides one of the attacker's bombs on a square. */
    record PlaceBomb(Position at) implements Effect {
    }
}
