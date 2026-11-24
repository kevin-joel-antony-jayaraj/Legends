package com.legends.game;

import com.legends.hero.HeroType;

/** What a player brings to a new game: their name and the hero they picked. */
public record PlayerChoice(String name, HeroType hero) {

    public PlayerChoice {
        if (hero == null) {
            throw new IllegalArgumentException("Hero choice is required");
        }
    }
}
