package com.legends.hero;

import com.legends.hero.meter.DamageTakenMeter;
import com.legends.hero.movement.KnightJump;
import com.legends.hero.movement.MovementPattern;

/**
 * The tank. L-jump movement, the Check landing attack, and a hidden Bomb Super that
 * charges from damage taken. Stats from docs/HEROES.md.
 */
public final class Knight extends Hero {

    public static final int MAX_HALF_HEARTS = Hearts.of(12);
    public static final int ACTIONS_PER_TURN = 2;
    public static final int DAMAGE_TO_CHARGE_SUPER = Hearts.of(6);

    private static final MovementPattern MOVEMENT = new KnightJump();

    public Knight(Player owner) {
        super(owner, MAX_HALF_HEARTS, new DamageTakenMeter(DAMAGE_TO_CHARGE_SUPER));
    }

    @Override
    public HeroType type() {
        return HeroType.KNIGHT;
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
