package com.legends.server;

import static org.assertj.core.api.Assertions.assertThat;

import com.legends.game.GameSetup;
import com.legends.game.GameState;
import com.legends.game.PlayerChoice;
import com.legends.hero.HeroType;
import com.legends.model.Position;
import com.legends.server.view.PlayerViews;
import java.util.List;
import org.junit.jupiter.api.Test;

/** The privacy rule: hidden bombs must never change what a player is shown. */
class PlayerViewsTest {

    private static final List<PlayerChoice> PLAYERS = List.of(
            new PlayerChoice("Ana", HeroType.ARCHER),
            new PlayerChoice("Ben", HeroType.KNIGHT),
            new PlayerChoice("Cleo", HeroType.CASTLE),
            new PlayerChoice("Dev", HeroType.PAWN));

    @Test
    void hiddenBombsDoNotShowUpInAnyOtherPlayersView() {
        GameState withoutBomb = new GameSetup().newGame(7, PLAYERS);
        GameState withBomb = new GameSetup().newGame(7, PLAYERS);
        Position somewhereFree = firstFreeSquare(withBomb);
        withBomb.placeBomb(somewhereFree, 1);

        for (int seat : new int[] {0, 2, 3}) {
            assertThat(PlayerViews.forSeat("g", withBomb, seat))
                    .as("seat %d", seat)
                    .isEqualTo(PlayerViews.forSeat("g", withoutBomb, seat));
        }
    }

    private static Position firstFreeSquare(GameState game) {
        for (int row = 0; row < game.board().rows(); row++) {
            for (int col = 0; col < game.board().cols(); col++) {
                Position p = new Position(row, col);
                if (game.isFreeToEnter(p)) {
                    return p;
                }
            }
        }
        throw new IllegalStateException("No free square");
    }
}
