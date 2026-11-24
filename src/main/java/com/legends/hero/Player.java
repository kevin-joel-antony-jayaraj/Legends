package com.legends.hero;

/**
 * A person playing the game.
 *
 * @param seat the player's place in the clockwise turn order, starting at 0
 * @param name display name
 */
public record Player(int seat, String name) {

    public Player {
        if (seat < 0) {
            throw new IllegalArgumentException("Seat must not be negative, got " + seat);
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Player name must not be blank");
        }
    }
}
