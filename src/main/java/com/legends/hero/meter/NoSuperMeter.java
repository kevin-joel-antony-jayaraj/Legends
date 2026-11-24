package com.legends.hero.meter;

/**
 * For heroes with no Super (the Pawn, whose evolution stages replace it).
 *
 * <p>This is the "null object" pattern: instead of a null meter that every caller must
 * check for, the Pawn gets a meter that simply never fills.
 */
public final class NoSuperMeter implements SuperMeter {

    @Override
    public void onHitDealt(int halfHearts) {
    }

    @Override
    public void onDamageTaken(int halfHearts) {
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public double progress() {
        return 0.0;
    }

    @Override
    public void reset() {
    }

    @Override
    public boolean hasSuper() {
        return false;
    }
}
