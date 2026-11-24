package com.legends.hero;

import com.legends.hero.meter.NoSuperMeter;
import com.legends.hero.movement.KingStep;
import com.legends.hero.movement.MovementPattern;

/**
 * The evolving hero. Starts as an Assassin with a Knife, becomes an Engineer after its
 * first hit, and a Queen after reaching the Keystone. It has no Super: evolution
 * replaces it. See docs/HEROES.md.
 *
 * <p>This class only tracks the stage. What triggers evolution (a hit, the Keystone)
 * is decided by the game rules, which call {@link #evolve()}.
 */
public final class Pawn extends Hero {

    public static final int ACTIONS_PER_TURN = 4;

    private static final MovementPattern MOVEMENT = new KingStep();

    private PawnStage stage = PawnStage.ASSASSIN;

    public Pawn(Player owner) {
        super(owner, PawnStage.ASSASSIN.maxHalfHearts(), new NoSuperMeter());
    }

    public PawnStage stage() {
        return stage;
    }

    /**
     * Moves to the next stage and gains its extra max hearts.
     *
     * @throws IllegalStateException if already a Queen
     */
    public void evolve() {
        PawnStage next = stage.next();
        raiseMaxHealth(next.maxHalfHearts());
        stage = next;
    }

    @Override
    public HeroType type() {
        return HeroType.PAWN;
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
    public String toString() {
        return super.toString() + " [" + stage + "]";
    }
}
