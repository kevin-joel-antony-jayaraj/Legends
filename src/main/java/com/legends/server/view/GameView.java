package com.legends.server.view;

import com.legends.model.Position;
import com.legends.model.TileType;
import java.util.List;

/**
 * The game as one player is allowed to see it.
 *
 * <p>This is the only shape of game state that leaves the server. It is built field by
 * field from public information, so secrets like hidden bombs can't leak by accident.
 * Moves and attacks are only filled in when it is this player's turn.
 *
 * @param board       tile types, row by row
 * @param keystone    where the Keystone is, or null if it isn't on the board
 * @param winnerSeat  null while the game is running, or if nobody survived
 */
public record GameView(
        String gameId,
        int yourSeat,
        List<List<TileType>> board,
        List<HeroView> heroes,
        Position keystone,
        int currentSeat,
        int actionsLeft,
        boolean over,
        Integer winnerSeat,
        List<Position> yourMoves,
        List<AttackChoice> yourAttacks) {
}
