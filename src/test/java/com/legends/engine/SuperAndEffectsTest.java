package com.legends.engine;

import static com.legends.testsupport.TestView.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.legends.board.Board;
import com.legends.engine.event.GameEvent;
import com.legends.game.GameSetup;
import com.legends.game.GameState;
import com.legends.game.Keystone;
import com.legends.game.PlayerChoice;
import com.legends.hero.Archer;
import com.legends.hero.Castle;
import com.legends.hero.Hearts;
import com.legends.hero.Hero;
import com.legends.hero.HeroType;
import com.legends.hero.Knight;
import com.legends.hero.Pawn;
import com.legends.hero.PawnStage;
import com.legends.model.Position;
import com.legends.model.TileType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Supers, the Knife throw, poison, bombs and the Keystone, on hand-built boards.
 * Seats: 0 Archer, 1 Knight, 2 Castle, 3 Pawn.
 */
class SuperAndEffectsTest {

    private static final List<PlayerChoice> PLAYERS = List.of(
            new PlayerChoice("Ana", HeroType.ARCHER),
            new PlayerChoice("Ben", HeroType.KNIGHT),
            new PlayerChoice("Cleo", HeroType.CASTLE),
            new PlayerChoice("Dev", HeroType.PAWN));

    private static GameEngine engine(Board board, List<Position> starts, int firstSeat) {
        return new GameEngine(new GameSetup().newGameOnBoard(board, PLAYERS, starts, firstSeat));
    }

    private static GameEngine engine(List<Position> starts, int firstSeat) {
        return engine(new Board(12, 12), starts, firstSeat);
    }

    private static Optional<Position> where(GameEngine engine, int seat) {
        return engine.game().positionOf(engine.game().heroInSeat(seat));
    }

    private static void chargeSuper(Hero hero) {
        while (!hero.isSuperReady()) {
            if (hero instanceof Knight) {
                hero.takeDamage(1);
            } else {
                hero.recordHitDealt(1);
            }
        }
    }

    /** Ends turns until it is {@code seat}'s turn. */
    private static List<GameEvent> passUntil(GameEngine engine, int seat) {
        List<GameEvent> all = new java.util.ArrayList<>();
        while (engine.game().currentSeat() != seat) {
            all.addAll(engine.perform(new EndTurnAction(engine.game().currentSeat())));
        }
        return all;
    }

    // ----- Supers -------------------------------------------------------------------

    @Test
    void superIsOnlyOfferedWhenCharged() {
        GameEngine engine = engine(List.of(at(5, 5), at(5, 7), at(10, 10), at(10, 1)), 0);
        Hero archer = engine.game().heroInSeat(0);
        assertFalse(engine.game().attackOptions(archer).stream().anyMatch(o -> o.attackId().equals("push-back")));
        chargeSuper(archer);
        assertTrue(engine.game().attackOptions(archer).stream().anyMatch(o -> o.attackId().equals("push-back")));
    }

    @Test
    void archerPushBackHitsPushesAndEmptiesTheMeter() {
        GameEngine engine = engine(List.of(at(5, 5), at(5, 7), at(10, 10), at(10, 1)), 0);
        Hero archer = engine.game().heroInSeat(0);
        chargeSuper(archer);

        List<GameEvent> events = engine.perform(new AttackAction(0, "push-back", at(5, 5)));

        assertEquals(new GameEvent.SuperUsed(0, "push-back"), events.get(0));
        assertEquals(Optional.of(at(5, 8)), where(engine, 1), "pushed just outside the 5x5 square");
        assertEquals(Hearts.of(12 - 1), engine.game().heroInSeat(1).halfHearts());
        assertFalse(archer.isSuperReady(), "using the Super empties the meter, and its own hit doesn't refill it");
    }

    @Test
    void castlePullBringsEnemiesIn() {
        GameEngine engine = engine(List.of(at(5, 9), at(1, 5), at(5, 5), at(10, 1)), 2);
        chargeSuper(engine.game().heroInSeat(2));
        engine.perform(new AttackAction(2, "pull", at(5, 5)));
        assertEquals(Optional.of(at(5, 6)), where(engine, 0));
        assertEquals(Optional.of(at(4, 5)), where(engine, 1));
    }

    // ----- Bombs --------------------------------------------------------------------

    @Test
    void bombIsHiddenAndExplodesWhenAnEnemyStepsOnIt() {
        GameEngine engine = engine(List.of(at(5, 5), at(1, 10), at(10, 10), at(10, 1)), 1);
        chargeSuper(engine.game().heroInSeat(1));

        List<GameEvent> placed = engine.perform(new AttackAction(1, "bomb", at(5, 6)));
        assertTrue(placed.contains(new GameEvent.BombPlaced(1)));
        assertTrue(placed.stream().noneMatch(e -> e.toString().contains("(5,6)")), "events must not reveal the bomb");

        passUntil(engine, 0);
        int before = engine.game().heroInSeat(0).halfHearts();
        List<GameEvent> stepped = engine.perform(new MoveAction(0, at(5, 6)));
        assertTrue(stepped.contains(new GameEvent.BombExploded(at(5, 6))));
        assertEquals(before - Knight.BOMB_DAMAGE, engine.game().heroInSeat(0).halfHearts());
        assertTrue(engine.game().hiddenBombs().isEmpty());
    }

    @Test
    void knightDoesNotTriggerOrGetHurtByItsOwnBombs() {
        GameEngine engine = engine(List.of(at(1, 1), at(5, 5), at(10, 10), at(10, 1)), 1);
        engine.game().placeBomb(at(3, 6), 1);
        int before = engine.game().heroInSeat(1).halfHearts();
        engine.perform(new MoveAction(1, at(3, 6)));
        assertEquals(before, engine.game().heroInSeat(1).halfHearts());
        assertEquals(1, engine.game().hiddenBombs().size(), "still armed");
    }

    @Test
    void bombsChainOnlyInsideTheBlastSquare() {
        // Bombs at (5,6), (5,7) and (5,9). The Archer steps on (5,6), which sets off (5,7)
        // next to it. (5,9) is 2 squares from (5,7), outside its 3x3, so it stays.
        // The Castle at (4,8) is inside (5,7)'s blast.
        GameEngine engine = engine(List.of(at(5, 5), at(1, 10), at(4, 8), at(10, 1)), 0);
        engine.game().placeBomb(at(5, 6), 1);
        engine.game().placeBomb(at(5, 7), 1);
        engine.game().placeBomb(at(5, 9), 1);

        List<GameEvent> events = engine.perform(new MoveAction(0, at(5, 6)));

        assertTrue(events.contains(new GameEvent.BombExploded(at(5, 6))));
        assertTrue(events.contains(new GameEvent.BombExploded(at(5, 7))));
        assertFalse(events.contains(new GameEvent.BombExploded(at(5, 9))));
        assertEquals(1, engine.game().hiddenBombs().size());
        assertEquals(Hearts.of(8 - 3 - 3), engine.game().heroInSeat(0).halfHearts(), "caught in both blasts");
        assertEquals(Hearts.of(10 - 3), engine.game().heroInSeat(2).halfHearts());
    }

    @Test
    void onlyOneBombFitsOnASquare() {
        GameEngine engine = engine(List.of(at(1, 1), at(5, 5), at(10, 10), at(10, 1)), 1);
        engine.game().placeBomb(at(6, 6), 1);
        Hero knight = engine.game().heroInSeat(1);
        chargeSuper(knight);

        // The square is still offered, so the list doesn't give the bomb away...
        assertTrue(engine.game().attackOptions(knight).stream()
                .anyMatch(o -> o.attackId().equals("bomb") && o.aim().equals(at(6, 6))));
        // ...but choosing it is refused, and costs nothing.
        assertEquals(Optional.of(AttackAction.BOMB_ALREADY_THERE), engine.check(new AttackAction(1, "bomb", at(6, 6))));
        assertTrue(knight.isSuperReady());
        assertEquals(1, engine.game().hiddenBombs().size());
    }

    @Test
    void teleportingQueenCanLandOnABomb() {
        // A 3x3 board: once the Pawn claims the Keystone, every free square has a bomb,
        // including (1,1), the square the Pawn just left.
        GameEngine engine = engine(new Board(3, 3), List.of(at(0, 0), at(0, 2), at(2, 2), at(1, 1)), 3);
        GameState game = engine.game();
        game.dropKeystone(at(1, 0), 3);
        for (Position p : List.of(at(0, 1), at(1, 1), at(1, 2), at(2, 0), at(2, 1))) {
            game.placeBomb(p, 1);
        }
        List<GameEvent> events = engine.perform(new MoveAction(3, at(1, 0)));
        assertTrue(events.contains(new GameEvent.KeystoneClaimed(3)));
        assertTrue(events.stream().anyMatch(GameEvent.BombExploded.class::isInstance), "landed on a bomb");
    }

    @Test
    void knockbackOntoABombSetsItOff() {
        // Knight at (5,5) checks the Archer at (4,5), knocking it onto the Knight's bomb at (3,5).
        GameEngine engine = engine(List.of(at(4, 5), at(5, 5), at(10, 10), at(10, 1)), 1);
        engine.game().placeBomb(at(3, 5), 1);
        engine.perform(new AttackAction(1, "check", at(4, 5)));
        assertEquals(Hearts.of(8 - 1 - 3), engine.game().heroInSeat(0).halfHearts(), "1 from Check, 3 from the bomb");
        assertEquals(Hearts.of(12), engine.game().heroInSeat(1).halfHearts(), "the Knight is immune");
    }

    // ----- Knife throw, poison and the Keystone -------------------------------------

    @Test
    void knifeThrowPoisonsDropsTheKeystoneAndEvolvesThePawn() {
        GameEngine engine = engine(List.of(at(0, 5), at(1, 10), at(10, 10), at(11, 5)), 3);
        List<GameEvent> events = engine.perform(new AttackAction(3, "knife-throw", at(0, 5)));

        Pawn pawn = (Pawn) engine.game().heroInSeat(3);
        assertFalse(pawn.hasKnife());
        assertEquals(PawnStage.ENGINEER, pawn.stage());
        assertEquals(Optional.of(new Keystone(at(0, 5), 3)), engine.game().keystone());
        assertTrue(events.contains(new GameEvent.HeroPoisoned(0, Pawn.POISON_TURNS)));
        assertTrue(engine.game().attackOptions(pawn).stream().noneMatch(o -> o.attackId().equals("knife-throw")),
                "one throw per game");
    }

    @Test
    void poisonTicksAtTheStartOfTheVictimsTurnsThenWearsOff() {
        GameEngine engine = engine(List.of(at(0, 5), at(1, 10), at(10, 10), at(11, 5)), 3);
        engine.perform(new AttackAction(3, "knife-throw", at(0, 5)));
        Hero archer = engine.game().heroInSeat(0);
        int afterThrow = archer.halfHearts();

        for (int turn = 1; turn <= Pawn.POISON_TURNS + 1; turn++) {
            List<GameEvent> events = passUntil(engine, 0);
            int expected = afterThrow - Math.min(turn, Pawn.POISON_TURNS) * Pawn.POISON_DAMAGE;
            assertEquals(expected, archer.halfHearts(), "after the Archer's turn " + turn + " starts");
            if (turn <= Pawn.POISON_TURNS) {
                assertTrue(events.contains(new GameEvent.HeroDamaged(0, 3, "poison", Pawn.POISON_DAMAGE, expected)));
            }
            engine.perform(new EndTurnAction(0));
        }
        assertTrue(archer.poison().isEmpty());
    }

    @Test
    void anyoneElseOnTheKeystoneIsFullyHealedAndItStays() {
        GameEngine engine = engine(List.of(at(5, 5), at(1, 10), at(10, 10), at(10, 1)), 0);
        engine.game().dropKeystone(at(5, 6), 3);
        engine.game().heroInSeat(0).takeDamage(Hearts.of(5));

        List<GameEvent> events = engine.perform(new MoveAction(0, at(5, 6)));

        assertEquals(Archer.MAX_HALF_HEARTS, engine.game().heroInSeat(0).halfHearts());
        assertTrue(events.contains(new GameEvent.HeroHealed(0, Hearts.of(5))));
        assertTrue(engine.game().keystone().isPresent(), "no cooldown, it stays");
    }

    @Test
    void thePawnClaimsTheKeystoneBecomesAQueenAndTeleports() {
        GameEngine engine = engine(List.of(at(1, 1), at(1, 10), at(10, 10), at(5, 5)), 3);
        engine.game().dropKeystone(at(5, 6), 3);
        Pawn pawn = (Pawn) engine.game().heroInSeat(3);

        List<GameEvent> events = engine.perform(new MoveAction(3, at(5, 6)));

        assertEquals(PawnStage.QUEEN, pawn.stage());
        assertEquals(Hearts.of(12), pawn.maxHalfHearts());
        assertTrue(engine.game().keystone().isEmpty());
        assertTrue(events.contains(new GameEvent.KeystoneClaimed(3)));
        Position landed = where(engine, 3).orElseThrow();
        assertFalse(landed.equals(at(5, 6)), "teleported away");
        assertTrue(engine.game().board().tileAt(landed).isWalkable());
    }

    @Test
    void teleportIsTheSameForTheSameSeed() {
        Position first = claimAndReport();
        Position second = claimAndReport();
        assertEquals(first, second);
    }

    private static Position claimAndReport() {
        GameEngine engine = engine(List.of(at(1, 1), at(1, 10), at(10, 10), at(5, 5)), 3);
        engine.game().dropKeystone(at(5, 6), 3);
        engine.perform(new MoveAction(3, at(5, 6)));
        return where(engine, 3).orElseThrow();
    }

    @Test
    void poisonCanFinishTheGame() {
        GameEngine engine = engine(List.of(at(0, 5), at(1, 10), at(10, 10), at(11, 5)), 3);
        GameState game = engine.game();
        game.heroInSeat(1).takeDamage(Hearts.of(12));
        game.heroInSeat(2).takeDamage(Hearts.of(10));
        game.heroInSeat(0).takeDamage(Hearts.of(7)); // Archer on 1 heart
        engine.perform(new AttackAction(3, "knife-throw", at(0, 5))); // throw does 1 heart: defeated
        assertTrue(game.isOver());
        assertEquals(3, game.winner().orElseThrow().owner().seat());
    }

    @Test
    void wallBlocksThePullButRiverOnlyStopsIt() {
        Board board = new Board(12, 12);
        board.setTile(at(3, 5), TileType.RIVER);
        GameEngine engine = engine(board, List.of(at(10, 1), at(1, 5), at(5, 5), at(11, 11)), 2);
        chargeSuper(engine.game().heroInSeat(2));
        engine.perform(new AttackAction(2, "pull", at(5, 5)));
        assertEquals(Optional.of(at(2, 5)), where(engine, 1), "stopped at the river's edge");
    }

    @Test
    void castleSuperChargesFromItsOwnHits() {
        GameEngine engine = engine(List.of(at(5, 8), at(1, 10), at(5, 5), at(10, 1)), 2);
        Castle castle = (Castle) engine.game().heroInSeat(2);
        for (int i = 0; i < Castle.HITS_TO_CHARGE_SUPER - 1; i++) {
            castle.recordHitDealt(1);
        }
        engine.perform(new AttackAction(2, "lane-shot", at(5, 8)));
        assertTrue(castle.isSuperReady());
    }
}
