package com.legends.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class PositionTest {

    @Test
    void stepMovesOneSquareInEachDirection() {
        Position start = new Position(5, 5);
        assertEquals(new Position(4, 5), start.step(Direction.N));
        assertEquals(new Position(4, 6), start.step(Direction.NE));
        assertEquals(new Position(5, 6), start.step(Direction.E));
        assertEquals(new Position(6, 6), start.step(Direction.SE));
        assertEquals(new Position(6, 5), start.step(Direction.S));
        assertEquals(new Position(6, 4), start.step(Direction.SW));
        assertEquals(new Position(5, 4), start.step(Direction.W));
        assertEquals(new Position(4, 4), start.step(Direction.NW));
    }

    @Test
    void offsetAddsAnyAmount() {
        assertEquals(new Position(3, 7), new Position(1, 5).offset(2, 2));
    }

    @Test
    void recordsWithSameCoordinatesAreEqual() {
        assertEquals(new Position(2, 3), new Position(2, 3));
    }

    @Test
    void directionKnowsOrthogonalAndDiagonal() {
        assertTrue(Direction.N.isOrthogonal());
        assertFalse(Direction.N.isDiagonal());
        assertTrue(Direction.NE.isDiagonal());
        assertFalse(Direction.NE.isOrthogonal());
    }
}
