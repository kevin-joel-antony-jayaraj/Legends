package com.legends.model;

import java.util.Random;
import java.util.SplittableRandom;

/**
 * Turns a game seed into a random number generator.
 *
 * <p>Why not just {@code new Random(seed)}? Java's {@link Random} gives nearly the same
 * first numbers for seeds that are close together (0, 1, 2...), so games with nearby
 * seeds would look alike. {@link SplittableRandom} scrambles the seed properly first.
 *
 * <p>The salt lets different parts of the game (board, setup) draw from different
 * streams from the same seed, so they don't reuse each other's numbers.
 */
public final class Seeds {

    private Seeds() {
    }

    /** A well-mixed {@link Random} for this seed and purpose. Same inputs, same numbers. */
    public static Random random(long seed, long salt) {
        return new Random(new SplittableRandom(seed ^ salt).nextLong());
    }
}
