package com.legends.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.legends.board.Board;
import com.legends.hero.Archer;
import com.legends.hero.Castle;
import com.legends.hero.Hero;
import com.legends.hero.Knight;
import com.legends.hero.Pawn;
import com.legends.hero.Player;
import com.legends.model.Position;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Uses a plain open board (not a generated one) so each test controls exactly where
 * everything is.
 */
class GameStateTest {

    private final Hero archer = new Archer(new Player(0, "Ana"));
    private final Hero knight = new Knight(new Player(1, "Ben"));
    private final Hero castle = new Castle(new Player(2, "Cleo"));
    private final Hero pawn = new Pawn(new Player(3, "Dev"));

    private final List<Position> starts =
            List.of(new Position(1, 1), new Position(1, 10), new Position(10, 10), new Position(10, 1));

    private GameState newGame(int firstSeat) {
        return new GameState(new Board(12, 12), 0L, List.of(archer, knight, castle, pawn), starts, firstSeat);
    }

    @Test
    void heroesStartOnTheirSquares() {
        GameState game = newGame(0);
        assertEquals(Optional.of(new Position(1, 1)), game.positionOf(archer));
        assertEquals(Optional.of(knight), game.heroAt(new Position(1, 10)));
        assertTrue(game.isOccupied(new Position(10, 10)));
        assertFalse(game.isOccupied(new Position(5, 5)));
    }

    @Test
    void firstPlayerGetsTheirActions() {
        GameState game = newGame(1);
        assertEquals(knight, game.currentHero());
        assertEquals(Knight.ACTIONS_PER_TURN, game.actionsLeft());
    }

    @Test
    void moveUpdatesPositionAndOccupancyTogether() {
        GameState game = newGame(0);
        Position to = new Position(2, 2);
        game.moveHero(archer, to);
        assertEquals(Optional.of(to), game.positionOf(archer));
        assertEquals(Optional.of(archer), game.heroAt(to));
        assertFalse(game.isOccupied(new Position(1, 1)), "old square is freed");
    }

    @Test
    void illegalMoveThrowsAndChangesNothing() {
        GameState game = newGame(0);
        assertThrows(IllegalArgumentException.class, () -> game.moveHero(archer, new Position(5, 5)));
        assertEquals(Optional.of(new Position(1, 1)), game.positionOf(archer));
    }

    @Test
    void heroesBlockEachOther() {
        GameState game = newGame(0);
        // Walk the Pawn up column 1 from (10,1) until it stands right below the Archer at (1,1).
        for (int row = 9; row >= 2; row--) {
            game.moveHero(pawn, new Position(row, 1));
        }
        assertTrue(game.legalMoves(archer).contains(new Position(2, 2)), "sanity: Archer can still move");
        assertFalse(game.legalMoves(archer).contains(new Position(2, 1)), "Pawn's square is taken");
        assertFalse(game.legalMoves(pawn).contains(new Position(1, 1)), "Archer's square is taken");
    }

    @Test
    void spendingMoreActionsThanAvailableThrows() {
        GameState game = newGame(0);
        for (int i = 0; i < Archer.ACTIONS_PER_TURN; i++) {
            game.spendAction();
        }
        assertEquals(0, game.actionsLeft());
        assertThrows(IllegalStateException.class, game::spendAction);
    }

    @Test
    void endTurnGoesClockwiseAndWraps() {
        GameState game = newGame(2);
        game.endTurn();
        assertEquals(pawn, game.currentHero());
        assertEquals(Pawn.ACTIONS_PER_TURN, game.actionsLeft());
        game.endTurn();
        assertEquals(archer, game.currentHero());
    }

    @Test
    void endTurnSkipsDeadHeroes() {
        GameState game = newGame(0);
        knight.takeDamage(knight.maxHalfHearts());
        game.endTurn();
        assertEquals(castle, game.currentHero());
    }

    @Test
    void deadHeroesAreRemovedAndFreeTheirSquare() {
        GameState game = newGame(0);
        castle.takeDamage(castle.maxHalfHearts());
        assertEquals(List.of(castle), game.removeDeadHeroes());
        assertTrue(game.positionOf(castle).isEmpty());
        assertFalse(game.isOccupied(new Position(10, 10)));
        assertTrue(game.legalMoves(castle).isEmpty());
        assertEquals(List.of(), game.removeDeadHeroes(), "removing twice does nothing");
    }

    @Test
    void lastHeroStandingWins() {
        GameState game = newGame(0);
        assertFalse(game.isOver());
        for (Hero hero : List.of(knight, castle, pawn)) {
            hero.takeDamage(hero.maxHalfHearts());
        }
        assertTrue(game.isOver());
        assertEquals(Optional.of(archer), game.winner());
    }

    @Test
    void startsMustBeFreeAndDistinct() {
        List<Position> clash = List.of(new Position(1, 1), new Position(1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new GameState(new Board(12, 12), 0L, List.of(archer, knight), clash, 0));
    }
}
