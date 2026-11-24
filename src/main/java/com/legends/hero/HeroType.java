package com.legends.hero;

/**
 * The kinds of hero a player can pick.
 *
 * <p>The factory method lives here so there is exactly one place that maps a choice
 * to a class. The switch is exhaustive: adding a new type without a class won't compile.
 */
public enum HeroType {
    ARCHER,
    KNIGHT,
    CASTLE,
    PAWN;

    /** Creates a new hero of this type for a player. */
    public Hero create(Player owner) {
        return switch (this) {
            case ARCHER -> new Archer(owner);
            case KNIGHT -> new Knight(owner);
            case CASTLE -> new Castle(owner);
            case PAWN -> new Pawn(owner);
        };
    }
}
