package com.legends.board;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.legends.model.Position;
import org.junit.jupiter.api.Test;

class CornerTest {

    @Test
    void localZeroIsTheCornerSquare() {
        assertEquals(new Position(0, 0), Corner.TOP_LEFT.toBoard(0, 0, 12, 12));
        assertEquals(new Position(0, 11), Corner.TOP_RIGHT.toBoard(0, 0, 12, 12));
        assertEquals(new Position(11, 0), Corner.BOTTOM_LEFT.toBoard(0, 0, 12, 12));
        assertEquals(new Position(11, 11), Corner.BOTTOM_RIGHT.toBoard(0, 0, 12, 12));
    }

    @Test
    void localCoordinatesGrowTowardTheMiddle() {
        assertEquals(new Position(2, 3), Corner.TOP_LEFT.toBoard(2, 3, 12, 12));
        assertEquals(new Position(2, 8), Corner.TOP_RIGHT.toBoard(2, 3, 12, 12));
        assertEquals(new Position(9, 3), Corner.BOTTOM_LEFT.toBoard(2, 3, 12, 12));
        assertEquals(new Position(9, 8), Corner.BOTTOM_RIGHT.toBoard(2, 3, 12, 12));
    }
}
