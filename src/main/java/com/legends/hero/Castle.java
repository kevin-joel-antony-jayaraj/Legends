package com.legends.hero;

import com.legends.hero.attack.Attack;
import com.legends.hero.attack.LineShot;
import com.legends.hero.meter.HitCountMeter;
import com.legends.hero.movement.MovementPattern;
import com.legends.hero.movement.OrthogonalSlide;
import com.legends.model.Direction;
import java.util.List;

/**
 * Lane controller. Slides like a rook, shoots in straight lines with unlimited range,
 * and pulls enemies in with its Super. Stats from docs/HEROES.md.
 */
public final class Castle extends Hero {

    public static final int MAX_HALF_HEARTS = Hearts.of(10);
    public static final int ACTIONS_PER_TURN = 3;
    public static final int MAX_SLIDE = 3;
    public static final int HITS_TO_CHARGE_SUPER = 4;

    public static final int CLOSE_RANGE = 2;
    public static final int CLOSE_DAMAGE = Hearts.of(2);
    public static final int FAR_DAMAGE = Hearts.of(0.5);

    /** Ranged, straight lines, unlimited range: 2 hearts within 2 squares, half a heart beyond. */
    private static final Attack LANE_SHOT = LineShot.ranged("lane-shot", Direction.ORTHOGONALS, LineShot.UNLIMITED,
            distance -> distance <= CLOSE_RANGE ? CLOSE_DAMAGE : FAR_DAMAGE);

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

    @Override
    public List<Attack> attacks() {
        return List.of(LANE_SHOT);
    }
}
