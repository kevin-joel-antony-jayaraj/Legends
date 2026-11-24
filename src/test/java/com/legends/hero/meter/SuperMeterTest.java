package com.legends.hero.meter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SuperMeterTest {

    @Test
    void hitCountMeterFillsAfterEnoughHits() {
        SuperMeter meter = new HitCountMeter(4);
        for (int i = 0; i < 3; i++) {
            meter.onHitDealt(2);
        }
        assertFalse(meter.isFull());
        assertEquals(0.75, meter.progress());
        meter.onHitDealt(1);
        assertTrue(meter.isFull());
    }

    @Test
    void hitCountMeterIgnoresDamageTakenAndZeroDamageHits() {
        SuperMeter meter = new HitCountMeter(1);
        meter.onDamageTaken(10);
        meter.onHitDealt(0);
        assertFalse(meter.isFull());
    }

    @Test
    void hitCountMeterDoesNotOverfill() {
        SuperMeter meter = new HitCountMeter(2);
        for (int i = 0; i < 5; i++) {
            meter.onHitDealt(2);
        }
        assertEquals(1.0, meter.progress());
    }

    @Test
    void damageTakenMeterFillsFromDamageTaken() {
        SuperMeter meter = new DamageTakenMeter(12);
        meter.onHitDealt(20);
        assertFalse(meter.isFull(), "dealing damage doesn't count");
        meter.onDamageTaken(6);
        assertEquals(0.5, meter.progress());
        meter.onDamageTaken(30);
        assertTrue(meter.isFull());
        assertEquals(1.0, meter.progress(), "doesn't overfill");
    }

    @Test
    void resetEmptiesTheMeter() {
        SuperMeter meter = new HitCountMeter(1);
        meter.onHitDealt(2);
        meter.reset();
        assertFalse(meter.isFull());
        assertEquals(0.0, meter.progress());
    }

    @Test
    void noSuperMeterNeverFills() {
        SuperMeter meter = new NoSuperMeter();
        meter.onHitDealt(100);
        meter.onDamageTaken(100);
        assertFalse(meter.isFull());
        assertFalse(meter.hasSuper());
    }
}
