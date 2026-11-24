package com.legends.game;

import com.legends.model.Position;

/**
 * A hidden bomb on the board.
 *
 * <p>Bombs are secret: no player, not even the one who placed it, can see where it is.
 * Only the server-side game state knows. Never send bomb positions to a client.
 *
 * @param at        the square it's on
 * @param ownerSeat the player who placed it (their hero takes no damage from it)
 */
public record Bomb(Position at, int ownerSeat) {
}
