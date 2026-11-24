package com.legends.hero;

/**
 * A poison on a hero: it loses some health at the start of each of its next few turns.
 *
 * @param turnsLeft         how many more turns it will tick
 * @param halfHeartsPerTurn damage each time it ticks
 * @param sourceSeat        who poisoned the hero, so damage can be credited to them
 */
public record PoisonStatus(int turnsLeft, int halfHeartsPerTurn, int sourceSeat) {

    public PoisonStatus {
        if (turnsLeft <= 0 || halfHeartsPerTurn <= 0) {
            throw new IllegalArgumentException("Poison needs positive turns and damage");
        }
    }

    /** The same poison with one turn used up, or null if this was the last turn. */
    PoisonStatus afterOneTurn() {
        return turnsLeft > 1 ? new PoisonStatus(turnsLeft - 1, halfHeartsPerTurn, sourceSeat) : null;
    }
}
