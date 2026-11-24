package com.legends.hero;

/**
 * A hero's hearts, stored in half-hearts.
 *
 * <p>The only way to change health is through these methods, which keep it between
 * 0 and the maximum. Nothing outside can set it to an invalid value.
 */
public final class Health {

    private int max;
    private int current;

    public Health(int maxHalfHearts) {
        if (maxHalfHearts <= 0) {
            throw new IllegalArgumentException("Max health must be positive, got " + maxHalfHearts);
        }
        this.max = maxHalfHearts;
        this.current = maxHalfHearts;
    }

    public int current() {
        return current;
    }

    public int max() {
        return max;
    }

    public boolean isAlive() {
        return current > 0;
    }

    /**
     * Removes health, never going below 0.
     *
     * @return how much was actually lost (less than asked if the hero had less left)
     */
    public int damage(int halfHearts) {
        requireNonNegative(halfHearts);
        int lost = Math.min(current, halfHearts);
        current -= lost;
        return lost;
    }

    /**
     * Restores health, never going above the maximum. A dead hero can't be healed.
     *
     * @return how much was actually restored
     */
    public int heal(int halfHearts) {
        requireNonNegative(halfHearts);
        if (!isAlive()) {
            return 0;
        }
        int restored = Math.min(max - current, halfHearts);
        current += restored;
        return restored;
    }

    /** Restores to full health. A dead hero can't be healed. */
    public void healFull() {
        if (isAlive()) {
            current = max;
        }
    }

    /**
     * Raises the maximum. The hero also gains the extra hearts, so raising 8 to 10
     * hearts while on 5 leaves them on 7.
     */
    public void raiseMax(int newMaxHalfHearts) {
        if (newMaxHalfHearts < max) {
            throw new IllegalArgumentException("New max " + newMaxHalfHearts + " is below current max " + max);
        }
        if (isAlive()) {
            current += newMaxHalfHearts - max;
        }
        max = newMaxHalfHearts;
    }

    private static void requireNonNegative(int halfHearts) {
        if (halfHearts < 0) {
            throw new IllegalArgumentException("Amount must not be negative, got " + halfHearts);
        }
    }
}
