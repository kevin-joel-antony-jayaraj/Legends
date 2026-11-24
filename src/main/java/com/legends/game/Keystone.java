package com.legends.game;

import com.legends.model.Position;

/**
 * The Keystone left by the Pawn's Knife throw. Unlike bombs, everyone can see it.
 *
 * @param at        the square it's on
 * @param ownerSeat the Pawn who threw the knife (only that Pawn can claim it to become a Queen)
 */
public record Keystone(Position at, int ownerSeat) {
}
