package com.legends.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.legends.model.Position;
import com.legends.model.TileType;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ReachabilityTest {

    @Test
    void openBoardIsFullyReachable() {
        Board board = new Board(5, 5);
        assertEquals(25, Reachability.walkableFrom(board, new Position(0, 0)).size());
    }

    @Test
    void diagonalGapLetsAKingThroughButNotAnOrthogonalWalker() {
        // Walls at (0,1) and (1,0) block every orthogonal path out of (0,0),
        // but a king can still step diagonally to (1,1).
        Board board = new Board(3, 3);
        board.setTile(new Position(0, 1), TileType.WALL);
        board.setTile(new Position(1, 0), TileType.WALL);
        Position start = new Position(0, 0);

        Set<Position> orthogonal = Reachability.walkableFrom(board, start);
        Set<Position> king = Reachability.reachableFrom(
                board, start, Reachability.ALL_DIRECTIONS, p -> board.tileAt(p).isWalkable());

        assertEquals(Set.of(start), orthogonal);
        assertTrue(king.contains(new Position(1, 1)));
    }

    @Test
    void startIsIncludedEvenIfItIsNotEnterable() {
        Board board = new Board(3, 3);
        board.setTile(new Position(1, 1), TileType.RIVER);
        assertTrue(Reachability.walkableFrom(board, new Position(1, 1)).contains(new Position(1, 1)));
    }

    @Test
    void neverLeavesTheBoard() {
        Board board = new Board(2, 2);
        Set<Position> reached = Reachability.walkableFrom(board, new Position(0, 0));
        assertFalse(reached.stream().anyMatch(p -> !board.contains(p)));
    }
}
