package com.legends.engine;

import static com.legends.testsupport.TestView.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.legends.board.Board;
import com.legends.engine.event.GameEvent;
import com.legends.game.GameSetup;
import com.legends.game.GameState;
import com.legends.game.PlayerChoice;
import com.legends.hero.Archer;
import com.legends.hero.Castle;
import com.legends.hero.Hearts;
import com.legends.hero.Hero;
import com.legends.hero.HeroType;
import com.legends.hero.Pawn;
import com.legends.hero.PawnStage;
import com.legends.hero.Player;
import com.legends.model.Position;
import com.legends.model.TileType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Engine tests on an open, hand-built board, so each test controls exactly where the
 * heroes stand. Seats: 0 Archer, 1 Knight, 2 Castle, 3 Pawn.
 */
class GameEngineTest {

    private static final List<PlayerChoice> PLAYERS = List.of(
            new PlayerChoice("Ana", HeroType.ARCHER),
            new PlayerChoice("Ben", HeroType.KNIGHT),
            new PlayerChoice("Cleo", HeroType.CASTLE),
            new PlayerChoice("Dev", HeroType.PAWN));

    private static final List<Position> CORNERS = List.of(at(1, 1), at(1, 10), at(10, 10), at(10, 1));

    private static GameEngine engine(Board board, List<Position> starts, int firstSeat) {
        return new GameEngine(new GameSetup().newGameOnBoard(board, PLAYERS, starts, firstSeat));
    }

    private static GameEngine engine(List<Position> starts, int firstSeat) {
        return engine(new Board(12, 12), starts, firstSeat);
    }

    private static <T extends GameEvent> List<T> eventsOf(List<GameEvent> events, Class<T> type) {
        return events.stream().filter(type::isInstance).map(type::cast).toList();
    }

    // ----- Turn rules ---------------------------------------------------------------

    @Test
    void moveSpendsOneAction() {
        GameEngine engine = engine(CORNERS, 0);
        List<GameEvent> events = engine.perform(new MoveAction(0, at(2, 2)));
        assertEquals(List.of(new GameEvent.HeroMoved(0, at(1, 1), at(2, 2))), events);
        assertEquals(Archer.ACTIONS_PER_TURN - 1, engine.game().actionsLeft());
    }

    @Test
    void actingOutOfTurnIsRejectedAndChangesNothing() {
        GameEngine engine = engine(CORNERS, 0);
        IllegalActionException e = assertThrows(IllegalActionException.class,
                () -> engine.perform(new MoveAction(1, at(3, 9))));
        assertEquals("It's not your turn", e.getMessage());
        assertEquals(Optional.of(at(1, 10)), engine.game().positionOf(engine.game().heroInSeat(1)));
    }

    @Test
    void illegalMoveIsRejectedWithAReason() {
        GameEngine engine = engine(CORNERS, 0);
        assertEquals(Optional.of("ARCHER can't move to (5,5)"), engine.check(new MoveAction(0, at(5, 5))));
        assertThrows(IllegalActionException.class, () -> engine.perform(new MoveAction(0, at(5, 5))));
        assertEquals(Archer.ACTIONS_PER_TURN, engine.game().actionsLeft(), "no action was spent");
    }

    @Test
    void turnPassesAutomaticallyWhenActionsRunOut() {
        GameEngine engine = engine(CORNERS, 0);
        engine.perform(new MoveAction(0, at(2, 2)));
        engine.perform(new MoveAction(0, at(3, 3)));
        List<GameEvent> events = engine.perform(new MoveAction(0, at(4, 4)));
        assertEquals(new GameEvent.TurnStarted(1, 2), events.get(events.size() - 1));
        assertEquals(1, engine.game().currentSeat());
    }

    @Test
    void endTurnHandsOverEarly() {
        GameEngine engine = engine(CORNERS, 0);
        List<GameEvent> events = engine.perform(new EndTurnAction(0));
        assertEquals(List.of(new GameEvent.TurnStarted(1, 2)), events);
    }

    @Test
    void heroWithNoLegalActionIsSkipped() {
        // Box the Knight in at (1,10): every L landing is water and nothing is in reach.
        Board board = new Board(12, 12);
        for (Position p : List.of(at(0, 8), at(2, 8), at(3, 9), at(3, 11))) {
            board.setTile(p, TileType.RIVER);
        }
        GameEngine engine = engine(board, CORNERS, 0);
        List<GameEvent> events = engine.perform(new EndTurnAction(0));
        assertEquals(List.of(new GameEvent.TurnSkipped(1), new GameEvent.TurnStarted(2, 3)), events);
    }

    // ----- Combat -------------------------------------------------------------------

    @Test
    void archerShotDealsDamageAndChargesTheSuper() {
        // Archer at (5,5), Knight diagonally at (3,3).
        GameEngine engine = engine(List.of(at(5, 5), at(3, 3), at(10, 10), at(10, 1)), 0);
        List<GameEvent> events = engine.perform(new AttackAction(0, "diagonal-shot", at(3, 3)));

        Hero knight = engine.game().heroInSeat(1);
        assertEquals(Hearts.of(12 - 2), knight.halfHearts());
        assertEquals(List.of(new GameEvent.HeroDamaged(1, 0, "diagonal-shot", Hearts.of(2), Hearts.of(10))),
                eventsOf(events, GameEvent.HeroDamaged.class));
        assertEquals(1.0 / Archer.HITS_TO_CHARGE_SUPER, engine.game().heroInSeat(0).superProgress());
    }

    @Test
    void attackOnAnEmptySquareIsRejected() {
        GameEngine engine = engine(CORNERS, 0);
        assertTrue(engine.check(new AttackAction(0, "diagonal-shot", at(3, 3))).isPresent());
    }

    @Test
    void castleShotIsWeakerFarAway() {
        // Castle at (5,5): Archer 2 squares up, Pawn 5 squares down.
        GameEngine engine = engine(List.of(at(3, 5), at(1, 10), at(5, 5), at(10, 5)), 2);
        engine.perform(new AttackAction(2, "lane-shot", at(3, 5)));
        engine.perform(new AttackAction(2, "lane-shot", at(10, 5)));
        assertEquals(Hearts.of(8 - 2), engine.game().heroInSeat(0).halfHearts());
        assertEquals(Hearts.of(8 - 0.5), engine.game().heroInSeat(3).halfHearts());
    }

    @Test
    void knightCheckTakesTheSquareKnocksBackAndSplashes() {
        // Knight at (5,5). Archer at the end of the L (3,6); Castle next to the Archer at (4,7).
        GameEngine engine = engine(List.of(at(3, 6), at(5, 5), at(4, 7), at(10, 1)), 1);
        List<GameEvent> events = engine.perform(new AttackAction(1, "check", at(3, 6), at(3, 6), at(3, 7)));

        GameState game = engine.game();
        assertEquals(Optional.of(at(3, 6)), game.positionOf(game.heroInSeat(1)), "Knight takes the square");
        assertEquals(Optional.of(at(3, 7)), game.positionOf(game.heroInSeat(0)), "Archer knocked east");
        assertEquals(Hearts.of(8 - 3), game.heroInSeat(0).halfHearts());
        assertEquals(Hearts.of(10 - 3), game.heroInSeat(2).halfHearts(), "splash");
        assertEquals(List.of(
                new GameEvent.HeroMoved(0, at(3, 6), at(3, 7)),
                new GameEvent.HeroMoved(1, at(5, 5), at(3, 6))), eventsOf(events, GameEvent.HeroMoved.class));
    }

    @Test
    void knightCheckWithTwoKnockbackDirectionsNeedsOneChosen() {
        GameEngine engine = engine(List.of(at(3, 6), at(5, 5), at(10, 10), at(10, 1)), 1);
        assertEquals(Optional.of("That check can be done more than one way; say where to land and push"),
                engine.check(new AttackAction(1, "check", at(3, 6))));
    }

    @Test
    void pawnEvolvesAfterItsFirstHit() {
        // Pawn at (5,5), Archer adjacent at (4,5).
        GameEngine engine = engine(List.of(at(4, 5), at(1, 10), at(10, 10), at(5, 5)), 3);
        List<GameEvent> events = engine.perform(new AttackAction(3, "knife", at(4, 5)));

        Pawn pawn = (Pawn) engine.game().heroInSeat(3);
        assertEquals(PawnStage.ENGINEER, pawn.stage());
        assertEquals(List.of(new GameEvent.PawnEvolved(3, PawnStage.ENGINEER)),
                eventsOf(events, GameEvent.PawnEvolved.class));
        assertEquals("ricochet", engine.game().attackOptions(pawn).get(0).attackId(), "new stage, new attack");
    }

    @Test
    void ricochetChainsToOtherPlayers() {
        // Engineer Pawn at (5,5). Archer adjacent at (4,5); Castle at (2,5), 2 squares from the Archer.
        GameEngine engine = engine(List.of(at(4, 5), at(1, 10), at(2, 5), at(5, 5)), 3);
        ((Pawn) engine.game().heroInSeat(3)).evolve();

        List<GameEvent> events = engine.perform(new AttackAction(3, "ricochet", at(4, 5)));

        assertEquals(Hearts.of(8 - 3), engine.game().heroInSeat(0).halfHearts(), "target takes 3");
        assertEquals(Hearts.of(10 - 1.5), engine.game().heroInSeat(2).halfHearts(), "chain takes 1.5");
        assertEquals(Hearts.of(12), engine.game().heroInSeat(1).halfHearts(), "the far Knight is untouched");
        assertEquals(2, eventsOf(events, GameEvent.HeroDamaged.class).size());
    }

    // ----- Defeat and winning -------------------------------------------------------

    @Test
    void defeatedHeroIsRemovedAndTheLastOneStandingWins() {
        GameEngine engine = engine(List.of(at(5, 5), at(4, 4), at(10, 10), at(10, 1)), 0);
        GameState game = engine.game();
        // Leave the Knight on 1 heart, and take the Castle and Pawn out of the game.
        game.heroInSeat(1).takeDamage(Hearts.of(11));
        game.heroInSeat(2).takeDamage(Hearts.of(10));
        game.heroInSeat(3).takeDamage(Hearts.of(8));

        List<GameEvent> events = engine.perform(new AttackAction(0, "diagonal-shot", at(4, 4)));

        assertTrue(events.contains(new GameEvent.HeroDefeated(1)));
        assertFalse(game.isOccupied(at(4, 4)), "the Knight's square is freed");
        assertEquals(new GameEvent.GameOver(0), events.get(events.size() - 1));
        assertEquals("The game is over", engine.check(new EndTurnAction(0)).orElseThrow());
    }

    @Test
    void damageIsCappedAtTheHeartsLeft() {
        GameEngine engine = engine(List.of(at(5, 5), at(4, 4), at(10, 10), at(10, 1)), 0);
        engine.game().heroInSeat(1).takeDamage(Hearts.of(11.5));
        List<GameEvent> events = engine.perform(new AttackAction(0, "diagonal-shot", at(4, 4)));
        assertEquals(Hearts.of(0.5), eventsOf(events, GameEvent.HeroDamaged.class).get(0).halfHearts(),
                "the Knight only had half a heart to lose");
    }

    @Test
    void attacksOnlyCountForTheSuperWhenTheyDealDamage() {
        Castle castle = new Castle(new Player(0, "x"));
        castle.recordHitDealt(0);
        assertEquals(0.0, castle.superProgress());
    }
}
