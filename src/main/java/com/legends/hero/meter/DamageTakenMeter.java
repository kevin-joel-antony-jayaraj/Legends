package com.legends.hero.meter;

/** Fills after the hero takes a set amount of damage. Used by the Knight (6 hearts). */
public final class DamageTakenMeter implements SuperMeter {

    private final int halfHeartsNeeded;
    private int taken;

    public DamageTakenMeter(int halfHeartsNeeded) {
        if (halfHeartsNeeded <= 0) {
            throw new IllegalArgumentException("Damage needed must be positive, got " + halfHeartsNeeded);
        }
        this.halfHeartsNeeded = halfHeartsNeeded;
    }

    @Override
    public void onHitDealt(int halfHearts) {
        // Dealing damage doesn't charge this meter.
    }

    @Override
    public void onDamageTaken(int halfHearts) {
        taken = Math.min(halfHeartsNeeded, taken + Math.max(0, halfHearts));
    }

    @Override
    public boolean isFull() {
        return taken >= halfHeartsNeeded;
    }

    @Override
    public double progress() {
        return (double) taken / halfHeartsNeeded;
    }

    @Override
    public void reset() {
        taken = 0;
    }
}
