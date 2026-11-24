package com.legends.hero.movement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.legends.board.Board;
import com.legends.board.BoardView;
import com.legends.model.Position;
import com.legends.model.TileType;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MovementPatternTest {

    /** A minimal BoardView for tests: a board plus a set of occupied squares. */
    private static final class TestView implements BoardView {
        private final Board board;
        private final Set<Position> occupied = new HashSet<>();

        TestView(int size) {
            this.board = new Board(size, size);
        }

        TestView tile(int row, int col, TileType type) {
            board.setTile(new Position(row, col), type);
            return this;
        }

        TestView occupy(int row, int col) {
            occupied.add(new Position(row, col));
            return this;
        }

        @Override
        public Board board() {
            return board;
        }

        @Override
        public boolean isOccupied(Position p) {
            return occupied.contains(p);
        }
    }

    private static Position at(int row, int col) {
        return new Position(row, col);
    }

    // ----- King step ----------------------------------------------------------------

    @Test
    void kingStepReachesAllEightNeighboursOnOpenGround() {
        assertEquals(8, new KingStep().destinations(at(5, 5), new TestView(12)).size());
    }

    @Test
    void kingStepInACornerHasThreeMoves() {
        assertEquals(Set.of(at(0, 1), at(1, 0), at(1, 1)),
                new KingStep().destinations(at(0, 0), new TestView(12)));
    }

    @Test
    void kingStepAvoidsRiverWallAndOccupiedSquares() {
        TestView view = new TestView(12)
                .tile(4, 5, TileType.RIVER)
                .tile(6, 5, TileType.WALL)
                .occupy(5, 6);
        Set<Position> moves = new KingStep().destinations(at(5, 5), view);
        assertEquals(5, moves.size());
        assertFalse(moves.contains(at(4, 5)));
        assertFalse(moves.contains(at(6, 5)));
        assertFalse(moves.contains(at(5, 6)));
    }

    @Test
    void kingStepCanEnterForestAndBridges() {
        TestView view = new TestView(12).tile(4, 5, TileType.FOREST).tile(6, 5, TileType.BRIDGE);
        Set<Position> moves = new KingStep().destinations(at(5, 5), view);
        assertTrue(moves.contains(at(4, 5)));
        assertTrue(moves.contains(at(6, 5)));
    }

    // ----- Knight jump --------------------------------------------------------------

    @Test
    void knightJumpHasEightLandingsOnOpenGround() {
        assertEquals(
                Set.of(at(3, 4), at(3, 6), at(4, 3), at(4, 7), at(6, 3), at(6, 7), at(7, 4), at(7, 6)),
                new KnightJump().destinations(at(5, 5), new TestView(12)));
    }

    @Test
    void knightJumpsOverRiversWallsAndPieces() {
        TestView view = new TestView(12)
                .tile(4, 5, TileType.RIVER)
                .tile(3, 5, TileType.WALL)
                .occupy(4, 4);
        assertTrue(new KnightJump().destinations(at(5, 5), view).contains(at(3, 4)));
    }

    @Test
    void knightCannotLandOnRiverWallOrPiece() {
        TestView view = new TestView(12)
                .tile(3, 4, TileType.RIVER)
                .tile(3, 6, TileType.WALL)
                .occupy(4, 3);
        Set<Position> moves = new KnightJump().destinations(at(5, 5), view);
        assertEquals(5, moves.size());
        assertFalse(moves.contains(at(3, 4)));
        assertFalse(moves.contains(at(3, 6)));
        assertFalse(moves.contains(at(4, 3)));
    }

    @Test
    void knightJumpStaysOnTheBoard() {
        assertEquals(Set.of(at(1, 2), at(2, 1)), new KnightJump().destinations(at(0, 0), new TestView(12)));
    }

    // ----- Orthogonal slide ---------------------------------------------------------

    @Test
    void slideGoesUpToMaxDistanceInFourDirections() {
        Set<Position> moves = new OrthogonalSlide(3).destinations(at(5, 5), new TestView(12));
        assertEquals(12, moves.size());
        assertTrue(moves.contains(at(2, 5)));
        assertFalse(moves.contains(at(1, 5)), "4 squares is too far");
        assertFalse(moves.contains(at(4, 4)), "no diagonals");
    }

    @Test
    void slideStopsBeforeTheFirstBlocker() {
        TestView view = new TestView(12)
                .tile(3, 5, TileType.RIVER) // two squares up
                .occupy(5, 7); // two squares right
        Set<Position> moves = new OrthogonalSlide(3).destinations(at(5, 5), view);
        assertTrue(moves.contains(at(4, 5)));
        assertFalse(moves.contains(at(3, 5)));
        assertFalse(moves.contains(at(2, 5)), "can't pass the river");
        assertTrue(moves.contains(at(5, 6)));
        assertFalse(moves.contains(at(5, 8)), "can't pass a piece");
    }

    @Test
    void slideCrossesBridges() {
        TestView view = new TestView(12).tile(4, 5, TileType.BRIDGE);
        assertTrue(new OrthogonalSlide(3).destinations(at(5, 5), view).contains(at(3, 5)));
    }

    @Test
    void slideRejectsZeroDistance() {
        assertThrows(IllegalArgumentException.class, () -> new OrthogonalSlide(0));
    }
}
