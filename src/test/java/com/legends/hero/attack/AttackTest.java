package com.legends.hero.attack;

import static com.legends.testsupport.TestView.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.legends.model.Direction;
import com.legends.model.Position;
import com.legends.model.TileType;
import com.legends.testsupport.TestView;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class AttackTest {

    private static final Attack DIAGONAL_SHOT = LineShot.ranged("shot", Direction.DIAGONALS, 4, d -> 4);
    private static final Attack KNIFE = LineShot.melee("knife", Direction.ALL, 2, d -> d == 1 ? 4 : 2);

    /** Damage per target square, for easy comparison. */
    private static Map<Position, Integer> damageByTarget(AttackOption option) {
        return option.hits().stream().collect(Collectors.toMap(Hit::target, Hit::halfHearts));
    }

    private static List<Position> aims(List<AttackOption> options) {
        return options.stream().map(AttackOption::aim).toList();
    }

    // ----- Ranged line shots --------------------------------------------------------

    @Test
    void rangedShotHitsOnlyTheFirstPieceInLine() {
        TestView view = new TestView(12).occupy(3, 3).occupy(2, 2);
        List<AttackOption> options = DIAGONAL_SHOT.options(at(5, 5), view);
        assertEquals(List.of(at(3, 3)), aims(options), "the piece at (2,2) is behind (3,3)");
    }

    @Test
    void rangedShotStopsAtItsRange() {
        TestView view = new TestView(12).occupy(0, 0);
        assertTrue(DIAGONAL_SHOT.options(at(5, 5), view).isEmpty(), "5 squares is out of range");
        assertEquals(1, DIAGONAL_SHOT.options(at(4, 4), view).size(), "4 squares is in range");
    }

    @Test
    void rangedShotFliesOverRiversButNotForestOrWalls() {
        TestView view = new TestView(12)
                .tile(4, 4, TileType.RIVER).occupy(3, 3)     // NW: over the river
                .tile(4, 6, TileType.FOREST).occupy(3, 7)    // NE: blocked by forest
                .tile(6, 6, TileType.WALL).occupy(7, 7);     // SE: blocked by wall
        assertEquals(List.of(at(3, 3)), aims(DIAGONAL_SHOT.options(at(5, 5), view)));
    }

    @Test
    void rangedShotCantHitAPieceInsideAForest() {
        TestView view = new TestView(12).tile(4, 4, TileType.FOREST).occupy(4, 4);
        assertTrue(DIAGONAL_SHOT.options(at(5, 5), view).isEmpty());
    }

    @Test
    void rangedShotCantBeFiredFromInsideAForest() {
        TestView view = new TestView(12).tile(5, 5, TileType.FOREST).occupy(4, 4);
        assertTrue(DIAGONAL_SHOT.options(at(5, 5), view).isEmpty());
    }

    @Test
    void lineShotDoesNotMoveTheAttacker() {
        TestView view = new TestView(12).occupy(4, 4);
        assertEquals(at(5, 5), DIAGONAL_SHOT.options(at(5, 5), view).get(0).endsAt());
    }

    @Test
    void castleStyleShotLosesDamageWithDistance() {
        Attack laneShot = LineShot.ranged("lane", Direction.ORTHOGONALS, LineShot.UNLIMITED, d -> d <= 2 ? 4 : 1);
        TestView view = new TestView(12).occupy(5, 7).occupy(5, 0);
        Map<Position, Integer> damage = laneShot.options(at(5, 5), view).stream()
                .collect(Collectors.toMap(AttackOption::aim, o -> o.hits().get(0).halfHearts()));
        assertEquals(Map.of(at(5, 7), 4, at(5, 0), 1), damage);
    }

    // ----- Melee with reach ---------------------------------------------------------

    @Test
    void knifeDamageDependsOnDistance() {
        TestView view = new TestView(12).occupy(4, 5).occupy(5, 7);
        Map<Position, Integer> damage = KNIFE.options(at(5, 5), view).stream()
                .collect(Collectors.toMap(AttackOption::aim, o -> o.hits().get(0).halfHearts()));
        assertEquals(Map.of(at(4, 5), 4, at(5, 7), 2), damage);
    }

    @Test
    void meleeReachesIntoForestButNotThroughWalls() {
        TestView view = new TestView(12)
                .tile(4, 5, TileType.FOREST).occupy(4, 5)
                .tile(5, 6, TileType.WALL).occupy(5, 7);
        assertEquals(List.of(at(4, 5)), aims(KNIFE.options(at(5, 5), view)));
    }

    @Test
    void meleeWorksFromInsideAForest() {
        TestView view = new TestView(12).tile(5, 5, TileType.FOREST).occupy(4, 5);
        assertEquals(1, KNIFE.options(at(5, 5), view).size());
    }

    // ----- Queen lines --------------------------------------------------------------

    @Test
    void queenLinesHitTheFirstPieceOnEveryLineAtOnce() {
        TestView view = new TestView(12).occupy(5, 8).occupy(2, 2).occupy(1, 1).occupy(5, 0);
        List<AttackOption> options = new QueenLines("queen", 4, 4).options(at(5, 5), view);
        assertEquals(1, options.size());
        assertEquals(Map.of(at(5, 8), 4, at(2, 2), 4), damageByTarget(options.get(0)),
                "(1,1) is behind (2,2) and (5,0) is out of range");
    }

    @Test
    void queenLinesWithNoTargetsHasNoOption() {
        assertTrue(new QueenLines("queen", 4, 4).options(at(5, 5), new TestView(12)).isEmpty());
    }

    // ----- Ricochet chain -----------------------------------------------------------

    @Test
    void ricochetHitsTheTargetThenEveryoneNearIt() {
        // Pawn at (5,5) hits (5,6). (5,8) is 2 from the target; (5,9) is 3 away.
        TestView view = new TestView(12).occupy(5, 6).occupy(5, 8).occupy(5, 9);
        List<AttackOption> options = new RicochetChain("ricochet", 6, 3, 2).options(at(5, 5), view);
        assertEquals(1, options.size());
        assertEquals(Map.of(at(5, 6), 6, at(5, 8), 3), damageByTarget(options.get(0)));
    }

    @Test
    void ricochetNeedsAnAdjacentTarget() {
        TestView view = new TestView(12).occupy(5, 7);
        assertTrue(new RicochetChain("ricochet", 6, 3, 2).options(at(5, 5), view).isEmpty());
    }

    // ----- Knight's Check --------------------------------------------------------

    private static final KnightCheck CHECK = new KnightCheck("check", new int[] {2, 4, 6}, 6);

    private static List<Effect> pushTo(Position from, Position to) {
        return List.of(new Effect.Push(from, to));
    }

    @Test
    void thereAreSixteenWaysToWalkAnL() {
        assertEquals(16, KnightCheck.allPaths(at(5, 5)).size());
    }

    @Test
    void earlyStopLandsOnTheTargetAndKnocksItBack() {
        TestView view = new TestView(12).occupy(4, 5);
        List<AttackOption> options = CHECK.options(at(5, 5), view);
        assertEquals(1, options.size(), "the same early stop reached by several paths is listed once");
        AttackOption option = options.get(0);
        assertEquals(at(4, 5), option.endsAt(), "the Knight takes the target's square");
        assertEquals(pushTo(at(4, 5), at(3, 5)), option.effects(), "knocked one square further, the way the Knight faces");
        assertEquals(Map.of(at(4, 5), 2), damageByTarget(option));
    }

    @Test
    void middleStopHitsForTwoHearts() {
        TestView view = new TestView(12).occupy(3, 5);
        AttackOption option = CHECK.options(at(5, 5), view).get(0);
        assertEquals(at(3, 5), option.endsAt());
        assertEquals(pushTo(at(3, 5), at(2, 5)), option.effects());
        assertEquals(Map.of(at(3, 5), 4), damageByTarget(option));
    }

    @Test
    void endOfTheLCanKnockTheTargetTwoWays() {
        // Reach (3,6) by going up 2 then right 1 (facing east) or right 1 then up 2 (facing north).
        TestView view = new TestView(12).occupy(3, 6);
        List<Position> knockedTo = CHECK.options(at(5, 5), view).stream()
                .filter(o -> o.aim().equals(at(3, 6)))
                .map(o -> o.firstPushDestination().orElseThrow()).toList();
        assertEquals(List.of(at(3, 7), at(2, 6)), knockedTo);
    }

    @Test
    void endStopSplashesEveryoneNextToTheTarget() {
        TestView view = new TestView(12).occupy(3, 6).occupy(4, 7);
        AttackOption option = CHECK.options(at(5, 5), view).stream()
                .filter(o -> o.aim().equals(at(3, 6))).findFirst().orElseThrow();
        assertEquals(Map.of(at(3, 6), 6, at(4, 7), 6), damageByTarget(option));
    }

    @Test
    void blockedKnockbackLeavesTheTargetAndTheKnightStopsShort() {
        // A wall behind the target: no knockback, so the Knight lands just before it.
        TestView view = new TestView(12).occupy(3, 5).tile(2, 5, TileType.WALL);
        AttackOption option = CHECK.options(at(5, 5), view).get(0);
        assertEquals(at(4, 5), option.endsAt());
        assertTrue(option.effects().isEmpty());
    }

    @Test
    void earlyStopWithBlockedKnockbackKeepsTheKnightInPlace() {
        TestView view = new TestView(12).occupy(4, 5).occupy(3, 5);
        AttackOption early = CHECK.options(at(5, 5), view).stream()
                .filter(o -> o.aim().equals(at(4, 5))).findFirst().orElseThrow();
        assertEquals(at(5, 5), early.endsAt());
    }

    @Test
    void noOptionWhenBothKnockbackAndLandingAreBlocked() {
        TestView view = new TestView(12).occupy(3, 5).tile(2, 5, TileType.RIVER).tile(4, 5, TileType.RIVER);
        assertTrue(CHECK.options(at(5, 5), view).isEmpty());
    }

    @Test
    void knightJumpsOverPiecesOnTheWay() {
        // The Knight jumps the piece on (4,5) to land on the middle stop (3,5).
        TestView view = new TestView(12).occupy(4, 5).occupy(3, 5);
        assertTrue(CHECK.options(at(5, 5), view).stream()
                .anyMatch(o -> o.aim().equals(at(3, 5)) && o.endsAt().equals(at(3, 5))));
    }

    // ----- Supers and the Knife throw -----------------------------------------------

    private static final PushBack SHOCKWAVE = new PushBack("push", 2, 2); // the Archer's 5x5

    @Test
    void pushBackHitsEveryoneInTheSquareAndPushesThemOut() {
        // Archer at (5,5): one piece in line to the east, one off-line to the north-west.
        TestView view = new TestView(12).occupy(5, 6).occupy(3, 4);
        AttackOption option = SHOCKWAVE.options(at(5, 5), view).get(0);
        assertEquals(Map.of(at(5, 6), 2, at(3, 4), 2), damageByTarget(option));
        assertTrue(option.effects().contains(new Effect.Push(at(5, 6), at(5, 8))), "out of the 5x5, just past its edge");
        assertTrue(option.effects().contains(new Effect.Push(at(3, 4), at(2, 3))), "diagonally away");
    }

    @Test
    void pushBackIgnoresPiecesOutsideTheSquare() {
        TestView view = new TestView(12).occupy(5, 8).occupy(2, 2);
        assertTrue(SHOCKWAVE.options(at(5, 5), view).isEmpty(), "3 squares away is outside the 5x5");
    }

    @Test
    void pushBackStopsAtTheRiverEdge() {
        TestView view = new TestView(12).occupy(5, 6).tile(5, 8, TileType.RIVER);
        assertEquals(List.of(new Effect.Push(at(5, 6), at(5, 7))), SHOCKWAVE.options(at(5, 5), view).get(0).effects());
    }

    @Test
    void pushBackMovesTheFarPieceFirstSoTheNearOneFollows() {
        // Two pieces in a row to the east: the far one moves out, making room for the near one,
        // which then stops behind it.
        TestView view = new TestView(12).occupy(5, 6).occupy(5, 7);
        List<Effect> pushes = SHOCKWAVE.options(at(5, 5), view).get(0).effects();
        assertEquals(List.of(new Effect.Push(at(5, 7), at(5, 8)), new Effect.Push(at(5, 6), at(5, 7))), pushes);
    }

    @Test
    void pushBackReachesIntoForest() {
        TestView view = new TestView(12).tile(5, 6, TileType.FOREST).occupy(5, 6);
        assertEquals(1, SHOCKWAVE.options(at(5, 5), view).size());
    }

    @Test
    void pullBringsTheFirstPieceOnEachLineNextToTheCastle() {
        TestView view = new TestView(12).occupy(5, 9).occupy(1, 5).occupy(5, 1);
        AttackOption option = new PullLines("pull").options(at(5, 5), view).get(0);
        assertTrue(option.hits().isEmpty(), "no damage");
        assertEquals(List.of(
                new Effect.Push(at(1, 5), at(4, 5)),
                new Effect.Push(at(5, 9), at(5, 6)),
                new Effect.Push(at(5, 1), at(5, 4))), option.effects());
    }

    @Test
    void pullStopsAtRiversAndIsBlockedByWalls() {
        TestView view = new TestView(12)
                .occupy(1, 5).tile(3, 5, TileType.RIVER)    // north: stops at the river's edge, (2,5)
                .occupy(5, 9).tile(5, 7, TileType.WALL);    // east: wall in the way, no pull
        AttackOption option = new PullLines("pull").options(at(5, 5), view).get(0);
        assertEquals(List.of(new Effect.Push(at(1, 5), at(2, 5))), option.effects());
    }

    @Test
    void pullWithNothingToMoveHasNoOption() {
        TestView view = new TestView(12).occupy(5, 6);
        assertTrue(new PullLines("pull").options(at(5, 5), view).isEmpty(), "already adjacent");
    }

    @Test
    void bombCanGoOnAnyEmptyWalkableSquare() {
        // Attacks can't see bombs, so squares with bombs are listed too; the engine rejects them.
        TestView view = new TestView(3).occupy(1, 1).tile(0, 0, TileType.RIVER);
        List<Position> squares = aims(new BombDrop("bomb").options(at(1, 1), view));
        assertEquals(7, squares.size(), "9 squares minus the Knight's own and the river");
        assertTrue(!squares.contains(at(0, 0)) && !squares.contains(at(1, 1)));
    }

    @Test
    void knifeThrowHitsAtAnyRangePoisonsAndDropsTheKeystone() {
        TestView view = new TestView(12).occupy(0, 5);
        AttackOption option = new KnifeThrow("throw", 2, 3, 2).options(at(11, 5), view).get(0);
        assertEquals(Map.of(at(0, 5), 2), damageByTarget(option));
        assertEquals(List.of(new Effect.Poison(at(0, 5), 3, 2), new Effect.DropKeystone(at(0, 5))), option.effects());
    }
}
