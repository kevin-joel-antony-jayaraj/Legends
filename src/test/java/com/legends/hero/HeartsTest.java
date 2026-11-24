package com.legends.hero;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class HeartsTest {

    @Test
    void convertsHeartsToHalfHearts() {
        assertEquals(0, Hearts.of(0));
        assertEquals(1, Hearts.of(0.5));
        assertEquals(3, Hearts.of(1.5));
        assertEquals(24, Hearts.of(12));
    }

    @Test
    void rejectsValuesThatAreNotWholeOrHalfHearts() {
        assertThrows(IllegalArgumentException.class, () -> Hearts.of(1.3));
        assertThrows(IllegalArgumentException.class, () -> Hearts.of(-1));
    }

    @Test
    void formatsForDisplay() {
        assertEquals("0", Hearts.format(0));
        assertEquals("3.5", Hearts.format(7));
        assertEquals("12", Hearts.format(24));
    }
}
