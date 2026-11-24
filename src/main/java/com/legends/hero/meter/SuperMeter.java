package com.legends.hero.meter;

/**
 * Tracks how close a hero is to using its Super.
 *
 * <p>Each hero charges differently (docs/HEROES.md), so this is an interface with one
 * implementation per charging rule. A hero tells its meter what happened, and the
 * meter decides whether that counts. The hero never needs to know the rule.
 */
public interface SuperMeter {

    /** Called when the hero lands a hit that dealt damage. */
    void onHitDealt(int halfHearts);

    /** Called when the hero loses health. */
    void onDamageTaken(int halfHearts);

    /** True when the Super can be used. */
    boolean isFull();

    /** How full the meter is, from 0.0 to 1.0. */
    double progress();

    /** Empties the meter after the Super is used. */
    void reset();

    /** False for heroes with no Super at all (the Pawn). */
    default boolean hasSuper() {
        return true;
    }
}
