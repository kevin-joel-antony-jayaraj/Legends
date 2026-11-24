package com.legends.hero;

import com.legends.hero.attack.Attack;
import com.legends.hero.attack.LineShot;
import com.legends.hero.attack.QueenLines;
import com.legends.hero.attack.RicochetChain;
import com.legends.hero.meter.NoSuperMeter;
import com.legends.hero.movement.KingStep;
import com.legends.hero.movement.MovementPattern;
import com.legends.model.Direction;
import java.util.List;

/**
 * The evolving hero. Starts as an Assassin with a Knife, becomes an Engineer after its
 * first hit, and a Queen after reaching the Keystone. It has no Super: evolution
 * replaces it. See docs/HEROES.md.
 *
 * <p>Its first hit evolves it to Engineer (see {@link #onHitDealt}). Reaching the
 * Keystone (a later version) evolves it to Queen through {@link #evolve()}.
 */
public final class Pawn extends Hero {

    public static final int ACTIONS_PER_TURN = 4;

    private static final MovementPattern MOVEMENT = new KingStep();

    /** Assassin: melee reach 2 in any direction, 2 hearts adjacent, 1 heart two squares away. */
    private static final Attack KNIFE = LineShot.melee("knife", Direction.ALL, 2,
            distance -> distance == 1 ? Hearts.of(2) : Hearts.of(1));

    /** Engineer: 3 hearts to an adjacent target, 1.5 hearts to everyone within 2 squares of it. */
    private static final Attack RICOCHET = new RicochetChain("ricochet", Hearts.of(3), Hearts.of(1.5), 2);

    /** Queen: first piece on each of the 8 lines, up to 4 squares, 2 hearts each. */
    private static final Attack QUEEN_LINES = new QueenLines("queen-lines", 4, Hearts.of(2));

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

    /** The Assassin's first hit evolves it to Engineer (docs/HEROES.md). */
    @Override
    protected void onHitDealt(int halfHearts) {
        if (halfHearts > 0 && stage == PawnStage.ASSASSIN) {
            evolve();
        }
    }

    @Override
    public List<Attack> attacks() {
        return switch (stage) {
            case ASSASSIN -> List.of(KNIFE);
            case ENGINEER -> List.of(RICOCHET);
            case QUEEN -> List.of(QUEEN_LINES);
        };
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
