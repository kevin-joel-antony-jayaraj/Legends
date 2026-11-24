package com.legends.hero;

import com.legends.hero.attack.Attack;
import com.legends.hero.attack.BombDrop;
import com.legends.hero.attack.KnightCheck;
import com.legends.hero.meter.DamageTakenMeter;
import com.legends.hero.movement.KnightJump;
import com.legends.hero.movement.MovementPattern;
import java.util.List;
import java.util.Optional;

/**
 * The tank. L-jump movement, the Check landing attack, and a hidden Bomb Super that
 * charges from damage taken. Stats from docs/HEROES.md.
 */
public final class Knight extends Hero {

    public static final int MAX_HALF_HEARTS = Hearts.of(12);
    public static final int ACTIONS_PER_TURN = 2;
    public static final int DAMAGE_TO_CHARGE_SUPER = Hearts.of(6);

    /** Check damage for each stop point along the L, and the splash when hitting the end. */
    public static final int CHECK_EARLY_DAMAGE = Hearts.of(1);
    public static final int CHECK_MIDDLE_DAMAGE = Hearts.of(2);
    public static final int CHECK_END_DAMAGE = Hearts.of(3);
    public static final int CHECK_SPLASH_DAMAGE = Hearts.of(3);

    private static final Attack CHECK = new KnightCheck("check",
            new int[] {CHECK_EARLY_DAMAGE, CHECK_MIDDLE_DAMAGE, CHECK_END_DAMAGE}, CHECK_SPLASH_DAMAGE);

    /** Bomb rules: damage per blast, blast size around the bomb, and how far a blast sets off other bombs. */
    public static final int BOMB_DAMAGE = Hearts.of(3);
    public static final int BOMB_BLAST_RADIUS = 1;
    public static final int BOMB_CHAIN_RADIUS = 2;

    /** Super: hide a bomb on any empty square. */
    private static final Attack BOMB = new BombDrop("bomb");

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

    @Override
    public List<Attack> attacks() {
        return List.of(CHECK);
    }

    @Override
    public Optional<Attack> superAttack() {
        return Optional.of(BOMB);
    }
}
