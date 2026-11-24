package com.legends.hero.meter;

/** Fills after a set number of hits. Used by the Archer (5 hits) and the Castle (4 hits). */
public final class HitCountMeter implements SuperMeter {

    private final int hitsNeeded;
    private int hits;

    public HitCountMeter(int hitsNeeded) {
        if (hitsNeeded <= 0) {
            throw new IllegalArgumentException("Hits needed must be positive, got " + hitsNeeded);
        }
        this.hitsNeeded = hitsNeeded;
    }

    @Override
    public void onHitDealt(int halfHearts) {
        if (halfHearts > 0 && hits < hitsNeeded) {
            hits++;
        }
    }

    @Override
    public void onDamageTaken(int halfHearts) {
        // Taking damage doesn't charge this meter.
    }

    @Override
    public boolean isFull() {
        return hits >= hitsNeeded;
    }

    @Override
    public double progress() {
        return (double) hits / hitsNeeded;
    }

    @Override
    public void reset() {
        hits = 0;
    }
}
