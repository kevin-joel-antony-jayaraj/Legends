package com.legends.hero;

import static com.legends.testsupport.TestView.at;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.legends.hero.attack.Attack;
import com.legends.hero.attack.AttackOption;
import com.legends.hero.movement.KingStep;
import com.legends.hero.movement.KnightJump;
import com.legends.hero.movement.OrthogonalSlide;
import com.legends.testsupport.TestView;
import java.util.List;
import org.junit.jupiter.api.Test;

class HeroTest {

    private static final Player OWNER = new Player(0, "Ana");

    @Test
    void statsMatchTheHeroesDoc() {
        assertHero(HeroType.ARCHER.create(OWNER), 8, 3);
        assertHero(HeroType.KNIGHT.create(OWNER), 12, 2);
        assertHero(HeroType.CASTLE.create(OWNER), 10, 3);
        assertHero(HeroType.PAWN.create(OWNER), 8, 4);
    }

    @Test
    void eachHeroHasItsMovementPattern() {
        assertInstanceOf(KingStep.class, HeroType.ARCHER.create(OWNER).movement());
        assertInstanceOf(KnightJump.class, HeroType.KNIGHT.create(OWNER).movement());
        assertInstanceOf(OrthogonalSlide.class, HeroType.CASTLE.create(OWNER).movement());
        assertInstanceOf(KingStep.class, HeroType.PAWN.create(OWNER).movement());
    }

    @Test
    void factoryCreatesTheRightTypeForEveryChoice() {
        for (HeroType type : HeroType.values()) {
            assertEquals(type, type.create(OWNER).type());
        }
    }

    @Test
    void archerChargesFromHitsNotDamageTaken() {
        Hero archer = new Archer(OWNER);
        archer.takeDamage(4);
        assertFalse(archer.isSuperReady());
        for (int i = 0; i < Archer.HITS_TO_CHARGE_SUPER; i++) {
            archer.recordHitDealt(4);
        }
        assertTrue(archer.isSuperReady());
    }

    @Test
    void castleChargesAfterFourHits() {
        Hero castle = new Castle(OWNER);
        for (int i = 0; i < 3; i++) {
            castle.recordHitDealt(1);
        }
        assertFalse(castle.isSuperReady());
        castle.recordHitDealt(1);
        assertTrue(castle.isSuperReady());
    }

    @Test
    void knightChargesFromDamageTaken() {
        Hero knight = new Knight(OWNER);
        knight.recordHitDealt(6);
        assertFalse(knight.isSuperReady(), "dealing damage doesn't charge the Knight");
        knight.takeDamage(Hearts.of(6));
        assertTrue(knight.isSuperReady());
    }

    @Test
    void knightOnlyChargesFromDamageActuallyLost() {
        Hero knight = new Knight(OWNER);
        knight.takeDamage(Hearts.of(12));
        assertFalse(knight.isAlive());
        assertEquals(0, knight.takeDamage(10), "a dead hero loses nothing more");
    }

    @Test
    void usingTheSuperEmptiesTheMeter() {
        Hero castle = new Castle(OWNER);
        for (int i = 0; i < Castle.HITS_TO_CHARGE_SUPER; i++) {
            castle.recordHitDealt(1);
        }
        castle.consumeSuper();
        assertFalse(castle.isSuperReady());
    }

    @Test
    void usingAnUnchargedSuperThrows() {
        assertThrows(IllegalStateException.class, () -> new Archer(OWNER).consumeSuper());
        assertThrows(IllegalStateException.class, () -> new Pawn(OWNER).consumeSuper());
    }

    @Test
    void pawnHasNoSuper() {
        assertFalse(new Pawn(OWNER).hasSuper());
    }

    @Test
    void pawnEvolvesThroughThreeStagesGainingHearts() {
        Pawn pawn = new Pawn(OWNER);
        assertEquals(PawnStage.ASSASSIN, pawn.stage());
        pawn.takeDamage(Hearts.of(3));

        pawn.evolve();
        assertEquals(PawnStage.ENGINEER, pawn.stage());
        assertEquals(Hearts.of(10), pawn.maxHalfHearts());
        assertEquals(Hearts.of(7), pawn.halfHearts(), "5 hearts left + 2 extra");

        pawn.evolve();
        assertEquals(PawnStage.QUEEN, pawn.stage());
        assertEquals(Hearts.of(12), pawn.maxHalfHearts());

        assertThrows(IllegalStateException.class, pawn::evolve);
    }

    @Test
    void archerShootsAcrossTheWholeBoard() {
        // Archer in one corner, a target in the opposite corner, nothing in between.
        TestView view = new TestView(12).occupy(0, 0).occupy(11, 11);
        Attack shot = new Archer(OWNER).attacks().stream()
                .filter(attack -> attack.id().equals("diagonal-shot")).findFirst().orElseThrow();
        assertEquals(List.of(at(0, 0)), shot.options(at(11, 11), view).stream().map(AttackOption::aim).toList());
    }

    @Test
    void healingIsCappedAtMax() {
        Hero archer = new Archer(OWNER);
        archer.takeDamage(Hearts.of(2));
        archer.healFull();
        assertEquals(archer.maxHalfHearts(), archer.halfHearts());
    }

    private static void assertHero(Hero hero, double hearts, int actions) {
        assertEquals(Hearts.of(hearts), hero.maxHalfHearts(), hero.type() + " hearts");
        assertEquals(Hearts.of(hearts), hero.halfHearts(), hero.type() + " starts full");
        assertEquals(actions, hero.actionsPerTurn(), hero.type() + " actions");
    }
}
