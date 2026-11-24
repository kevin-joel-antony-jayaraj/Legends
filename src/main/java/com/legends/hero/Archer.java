package com.legends.hero;

import com.legends.hero.meter.HitCountMeter;
import com.legends.hero.movement.KingStep;
import com.legends.hero.movement.MovementPattern;

/**
 * Ranged hero. Diagonal shots, short orthogonal melee, and a Push Back Super.
 * Stats from docs/HEROES.md.
 */
public final class Archer extends Hero {

    public static final int MAX_HALF_HEARTS = Hearts.of(8);
    public static final int ACTIONS_PER_TURN = 3;
    public static final int HITS_TO_CHARGE_SUPER = 5;

    private static final MovementPattern MOVEMENT = new KingStep();

    public Archer(Player owner) {
        super(owner, MAX_HALF_HEARTS, new HitCountMeter(HITS_TO_CHARGE_SUPER));
    }

    @Override
    public HeroType type() {
        return HeroType.ARCHER;
    }

    @Override
    public int actionsPerTurn() {
        return ACTIONS_PER_TURN;
    }

    @Override
    public MovementPattern movement() {
        return MOVEMENT;
    }
}
