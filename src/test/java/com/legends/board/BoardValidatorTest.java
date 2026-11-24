package com.legends.board;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.legends.model.Position;
import com.legends.model.TileType;
import java.util.List;
import org.junit.jupiter.api.Test;

class BoardValidatorTest {

    private static final List<Position> STARTS =
            List.of(new Position(1, 1), new Position(1, 10), new Position(10, 1), new Position(10, 10));

    @Test
    void openBoardIsValid() {
        assertTrue(BoardValidator.validate(new Board(12, 12), STARTS).isValid());
    }

    @Test
    void walledInStartIsInvalid() {
        Board board = new Board(12, 12);
        // Box in the top-left start with walls on rows/columns 0-2.
        for (int i = 0; i <= 2; i++) {
            board.setTile(new Position(2, i), TileType.WALL);
            board.setTile(new Position(i, 2), TileType.WALL);
        }
        ValidationResult result = BoardValidator.validate(board, STARTS);
        assertFalse(result.isValid());
        assertTrue(result.problems().stream().anyMatch(p -> p.contains("can't be reached")), result.problems().toString());
    }

    @Test
    void startOnRiverIsInvalid() {
        Board board = new Board(12, 12);
        board.setTile(new Position(1, 1), TileType.RIVER);
        assertFalse(BoardValidator.validate(board, STARTS).isValid());
    }

    @Test
    void startWithNoWalkableNeighbourIsInvalid() {
        Board board = new Board(12, 12);
        board.setTile(new Position(0, 1), TileType.WALL);
        board.setTile(new Position(2, 1), TileType.WALL);
        board.setTile(new Position(1, 0), TileType.WALL);
        board.setTile(new Position(1, 2), TileType.WALL);
        assertFalse(BoardValidator.validate(board, STARTS).isValid());
    }

    @Test
    void duplicateStartsAreInvalid() {
        List<Position> starts = List.of(new Position(1, 1), new Position(1, 1));
        assertFalse(BoardValidator.validate(new Board(12, 12), starts).isValid());
    }

    @Test
    void bridgeConnectsAcrossARiver() {
        Board board = new Board(12, 12);
        for (int c = 0; c < 12; c++) {
            board.setTile(new Position(6, c), TileType.RIVER);
        }
        assertFalse(BoardValidator.validate(board, STARTS).isValid(), "river splits the board");

        board.setTile(new Position(6, 5), TileType.BRIDGE);
        assertTrue(BoardValidator.validate(board, STARTS).isValid(), "bridge reconnects it");
    }
}
