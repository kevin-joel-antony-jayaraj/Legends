package com.legends.hero;

import com.legends.hero.attack.Attack;
import com.legends.hero.attack.LineShot;
import com.legends.hero.attack.PushBack;
import com.legends.hero.meter.HitCountMeter;
import com.legends.hero.movement.KingStep;
import com.legends.hero.movement.MovementPattern;
import com.legends.model.Direction;
import java.util.List;
import java.util.Optional;

/**
 * Ranged hero. Diagonal shots, short orthogonal melee, and a Push Back Super.
 * Stats from docs/HEROES.md.
 */
public final class Archer extends Hero {

    public static final int MAX_HALF_HEARTS = Hearts.of(8);
    public static final int ACTIONS_PER_TURN = 3;
    public static final int HITS_TO_CHARGE_SUPER = 5;

    public static final int SHOT_RANGE = 4;
    public static final int SHOT_DAMAGE = Hearts.of(2);
    public static final int MELEE_DAMAGE = Hearts.of(2);

    /** Ranged, along the diagonals, up to 4 squares. */
    private static final Attack DIAGONAL_SHOT =
            LineShot.ranged("diagonal-shot", Direction.DIAGONALS, SHOT_RANGE, distance -> SHOT_DAMAGE);

    /** Melee, 1 square up, down, left or right. */
    private static final Attack MELEE =
            LineShot.melee("melee", Direction.ORTHOGONALS, 1, distance -> MELEE_DAMAGE);

    public static final int PUSH_RANGE = 4;
    public static final int PUSH_SQUARES = 3;
    public static final int PUSH_DAMAGE = Hearts.of(1);

    /** Super: hit the first piece in any direction within 4 squares and push it 3 squares away. */
    private static final Attack PUSH_BACK = new PushBack("push-back", PUSH_RANGE, PUSH_SQUARES, PUSH_DAMAGE);

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

    @Override
    public List<Attack> attacks() {
        return List.of(DIAGONAL_SHOT, MELEE);
    }

    @Override
    public Optional<Attack> superAttack() {
        return Optional.of(PUSH_BACK);
    }
}
