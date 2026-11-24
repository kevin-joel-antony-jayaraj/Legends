package com.legends.hero;

/**
 * The Pawn's three evolution stages, in order. Each stage raises its max hearts.
 * See docs/HEROES.md.
 */
public enum PawnStage {
    ASSASSIN(Hearts.of(8)),
    ENGINEER(Hearts.of(10)),
    QUEEN(Hearts.of(12));

    private final int maxHalfHearts;

    PawnStage(int maxHalfHearts) {
        this.maxHalfHearts = maxHalfHearts;
    }

    public int maxHalfHearts() {
        return maxHalfHearts;
    }

    public boolean isFinal() {
        return this == QUEEN;
    }

    /**
     * The stage after this one.
     *
     * @throws IllegalStateException if this is already the final stage
     */
    public PawnStage next() {
        if (isFinal()) {
            throw new IllegalStateException("QUEEN is the final Pawn stage");
        }
        return values()[ordinal() + 1];
    }
}
