package com.legends.server;

import static org.assertj.core.api.Assertions.assertThat;

import com.legends.board.Board;
import com.legends.game.GameSetup;
import com.legends.game.GameState;
import com.legends.game.PlayerChoice;
import com.legends.hero.Hero;
import com.legends.hero.HeroType;
import com.legends.model.Position;
import com.legends.server.view.AttackChoice;
import com.legends.server.view.PlayerViews;
import java.util.List;
import org.junit.jupiter.api.Test;

/** What each player is shown: secrets stay hidden, and attack options carry what the board needs to preview them. */
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

    @Test
    void anAttackOptionListsEveryPieceItMoves() {
        // Castle at (10,10) with its Super charged; the Archer up its column at (2,10) gets pulled next to it.
        Board open = new Board(12, 12);
        GameState game = new GameSetup().newGameOnBoard(open, PLAYERS,
                List.of(new Position(2, 10), new Position(0, 0), new Position(10, 10), new Position(11, 0)), 2);
        Hero castle = game.heroInSeat(2);
        while (!castle.isSuperReady()) {
            castle.recordHitDealt(1);
        }

        AttackChoice pull = PlayerViews.forSeat("g", game, 2).yourAttacks().stream()
                .filter(choice -> choice.attackId().equals("pull"))
                .findFirst().orElseThrow();

        assertThat(pull.isSuper()).isTrue();
        assertThat(pull.pushes()).containsExactly(new AttackChoice.PushView(new Position(2, 10), new Position(9, 10)));
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
