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

    // ----- Knight's Check -----------------------------------------------------------

    private static final KnightCheck CHECK = new KnightCheck("check", new int[] {2, 4, 6}, 6);

    @Test
    void thereAreSixteenWaysToWalkAnL() {
        assertEquals(16, KnightCheck.allPaths(at(5, 5)).size());
    }

    @Test
    void earlyStopHitsForOneHeartAndTheKnightStaysPut() {
        TestView view = new TestView(12).occupy(4, 5);
        List<AttackOption> options = CHECK.options(at(5, 5), view);
        assertEquals(1, options.size(), "the same early stop reached by several paths is listed once");
        AttackOption option = options.get(0);
        assertEquals(at(5, 5), option.endsAt());
        assertEquals(Map.of(at(4, 5), 2), damageByTarget(option));
    }

    @Test
    void middleStopHitsForTwoHeartsAndLandsJustBefore() {
        TestView view = new TestView(12).occupy(3, 5);
        AttackOption option = CHECK.options(at(5, 5), view).get(0);
        assertEquals(at(4, 5), option.endsAt());
        assertEquals(Map.of(at(3, 5), 4), damageByTarget(option));
    }

    @Test
    void endStopHitsForThreeHeartsAndSplashesNeighbours() {
        // Target at the end of the L (3,6); (2,6) and (3,7) stand next to it.
        TestView view = new TestView(12).occupy(3, 6).occupy(2, 6).occupy(3, 7);
        AttackOption viaStraight = CHECK.options(at(5, 5), view).stream()
                .filter(o -> o.aim().equals(at(3, 6)) && o.endsAt().equals(at(3, 5)))
                .findFirst().orElseThrow();
        assertEquals(Map.of(at(3, 6), 6, at(2, 6), 6, at(3, 7), 6), damageByTarget(viaStraight));
    }

    @Test
    void endOfTheLCanBeReachedTwoWays() {
        TestView view = new TestView(12).occupy(3, 6);
        List<Position> landings = CHECK.options(at(5, 5), view).stream()
                .filter(o -> o.aim().equals(at(3, 6)))
                .map(AttackOption::endsAt).toList();
        assertEquals(List.of(at(3, 5), at(4, 6)), landings, "via the bend at (3,5) or the diagonal (4,6)");
    }

    @Test
    void knightCantLandInARiver() {
        TestView view = new TestView(12).occupy(3, 5).tile(4, 5, TileType.RIVER);
        assertTrue(CHECK.options(at(5, 5), view).isEmpty(), "the only landing square is water");
    }

    @Test
    void knightJumpsOverPiecesOnTheWay() {
        // Path (4,5) -> (3,5) -> (3,6): the piece on (4,5) is jumped over, and the
        // Knight lands on the free bend (3,5) to hit the end of the L.
        TestView view = new TestView(12).occupy(4, 5).occupy(3, 6);
        assertTrue(CHECK.options(at(5, 5), view).stream()
                .anyMatch(o -> o.aim().equals(at(3, 6)) && o.endsAt().equals(at(3, 5))));
    }
}
