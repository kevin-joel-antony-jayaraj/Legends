package com.legends.hero;

import com.legends.hero.meter.HitCountMeter;
import com.legends.hero.movement.MovementPattern;
import com.legends.hero.movement.OrthogonalSlide;

/**
 * Lane controller. Slides like a rook, shoots in straight lines with unlimited range,
 * and pulls enemies in with its Super. Stats from docs/HEROES.md.
 */
public final class Castle extends Hero {

    public static final int MAX_HALF_HEARTS = Hearts.of(10);
    public static final int ACTIONS_PER_TURN = 3;
    public static final int MAX_SLIDE = 3;
    public static final int HITS_TO_CHARGE_SUPER = 4;

    private static final MovementPattern MOVEMENT = new OrthogonalSlide(MAX_SLIDE);

    public Castle(Player owner) {
        super(owner, MAX_HALF_HEARTS, new HitCountMeter(HITS_TO_CHARGE_SUPER));
    }

    @Override
    public HeroType type() {
        return HeroType.CASTLE;
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
