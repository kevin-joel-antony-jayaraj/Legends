package com.legends.hero;

/**
 * Converts between hearts (what the rules talk about) and half-hearts (what the code stores).
 *
 * <p>All health and damage are stored as whole numbers of half-hearts, so 1.5 hearts
 * is 3. Whole numbers avoid floating-point rounding bugs like 0.1 + 0.2 != 0.3.
 */
public final class Hearts {

    public static final int HALF_HEARTS_PER_HEART = 2;

    private Hearts() {
    }

    /**
     * Converts hearts to half-hearts. Only whole and half hearts are allowed.
     *
     * @throws IllegalArgumentException for negative values or values like 1.3
     */
    public static int of(double hearts) {
        double halves = hearts * HALF_HEARTS_PER_HEART;
        if (hearts < 0 || halves != Math.rint(halves)) {
            throw new IllegalArgumentException("Hearts must be a non-negative multiple of 0.5, got " + hearts);
        }
        return (int) halves;
    }

    /** Formats half-hearts for display, for example 7 becomes "3.5". */
    public static String format(int halfHearts) {
        int whole = halfHearts / HALF_HEARTS_PER_HEART;
        return halfHearts % HALF_HEARTS_PER_HEART == 0 ? Integer.toString(whole) : whole + ".5";
    }
}
