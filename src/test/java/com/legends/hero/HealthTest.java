package com.legends.hero;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HealthTest {

    @Test
    void startsFull() {
        Health health = new Health(16);
        assertEquals(16, health.current());
        assertTrue(health.isAlive());
    }

    @Test
    void damageNeverGoesBelowZero() {
        Health health = new Health(4);
        assertEquals(4, health.damage(10), "only 4 could actually be lost");
        assertEquals(0, health.current());
        assertFalse(health.isAlive());
    }

    @Test
    void healNeverGoesAboveMax() {
        Health health = new Health(10);
        health.damage(3);
        assertEquals(3, health.heal(8));
        assertEquals(10, health.current());
    }

    @Test
    void deadHeroCannotBeHealed() {
        Health health = new Health(4);
        health.damage(4);
        assertEquals(0, health.heal(4));
        health.healFull();
        assertEquals(0, health.current());
    }

    @Test
    void raiseMaxAlsoGivesTheExtraHearts() {
        Health health = new Health(16);
        health.damage(6);
        health.raiseMax(20);
        assertEquals(20, health.max());
        assertEquals(14, health.current());
    }

    @Test
    void rejectsBadValues() {
        assertThrows(IllegalArgumentException.class, () -> new Health(0));
        assertThrows(IllegalArgumentException.class, () -> new Health(4).damage(-1));
        assertThrows(IllegalArgumentException.class, () -> new Health(4).heal(-1));
        assertThrows(IllegalArgumentException.class, () -> new Health(4).raiseMax(2));
    }
}
