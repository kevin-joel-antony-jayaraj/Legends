package com.legends.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.legends.model.Position;
import com.legends.model.TileType;
import org.junit.jupiter.api.Test;

class BoardTest {

    @Test
    void newBoardIsFilledWithGrass() {
        Board board = new Board(12, 12);
        assertEquals(TileType.GRASS, board.tileAt(new Position(0, 0)));
        assertEquals(TileType.GRASS, board.tileAt(new Position(11, 11)));
    }

    @Test
    void reportsItsSize() {
        Board board = new Board(12, 10);
        assertEquals(12, board.rows());
        assertEquals(10, board.cols());
    }

    @Test
    void containsIsFalseOutsideTheEdges() {
        Board board = new Board(12, 12);
        assertTrue(board.contains(new Position(0, 0)));
        assertTrue(board.contains(new Position(11, 11)));
        assertFalse(board.contains(new Position(-1, 0)));
        assertFalse(board.contains(new Position(0, 12)));
        assertFalse(board.contains(new Position(12, 0)));
    }

    @Test
    void setTileChangesOnlyThatSquare() {
        Board board = new Board(12, 12);
        board.setTile(new Position(3, 4), TileType.RIVER);
        assertEquals(TileType.RIVER, board.tileAt(new Position(3, 4)));
        assertEquals(TileType.GRASS, board.tileAt(new Position(3, 5)));
    }

    @Test
    void tileAtOutsideTheBoardThrows() {
        Board board = new Board(12, 12);
        assertThrows(IllegalArgumentException.class, () -> board.tileAt(new Position(12, 0)));
    }

    @Test
    void setTileOutsideTheBoardThrows() {
        Board board = new Board(12, 12);
        assertThrows(IllegalArgumentException.class, () -> board.setTile(new Position(0, -1), TileType.WALL));
    }

    @Test
    void zeroSizedBoardIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Board(0, 12));
    }
}
