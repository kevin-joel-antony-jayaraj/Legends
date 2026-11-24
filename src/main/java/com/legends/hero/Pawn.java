package com.legends.hero;

import com.legends.hero.attack.Attack;
import com.legends.hero.attack.KnifeThrow;
import com.legends.hero.attack.LineShot;
import com.legends.hero.attack.QueenLines;
import com.legends.hero.attack.RicochetChain;
import com.legends.hero.meter.NoSuperMeter;
import com.legends.hero.movement.KingStep;
import com.legends.hero.movement.MovementPattern;
import com.legends.model.Direction;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The evolving hero. Starts as an Assassin with a Knife, becomes an Engineer after its
 * first hit, and a Queen after reaching the Keystone. It has no Super: evolution
 * replaces it. See docs/HEROES.md.
 *
 * <p>Its first hit evolves it to Engineer (see {@link #onHitDealt}). Its one-per-game
 * Knife throw leaves the Keystone on the board, and reaching the Keystone makes it a Queen
 * (see {@link #becomeQueen()}).
 */
public final class Pawn extends Hero {

    public static final int ACTIONS_PER_TURN = 4;

    private static final MovementPattern MOVEMENT = new KingStep();

    /** Assassin: melee reach 2 in any direction, 2 hearts adjacent, 1 heart two squares away. */
    private static final Attack KNIFE = LineShot.melee("knife", Direction.ALL, 2,
            distance -> distance == 1 ? Hearts.of(2) : Hearts.of(1));

    /**
     * Engineer: a shot along any line, up to 4 squares, that ricochets to the nearest enemy
     * within 2 squares of the last one hit, doubling each time: 2, 4, then 8 hearts.
     */
    public static final int RICOCHET_RANGE = 4;
    public static final int RICOCHET_FIRST_DAMAGE = Hearts.of(2);
    public static final int RICOCHET_BOUNCE_RADIUS = 2;
    public static final int RICOCHET_MAX_HITS = 3;
    private static final Attack RICOCHET = new RicochetChain("ricochet",
            RICOCHET_RANGE, RICOCHET_FIRST_DAMAGE, RICOCHET_BOUNCE_RADIUS, RICOCHET_MAX_HITS);

    /** Queen: first piece on each of the 8 lines, up to 4 squares, 2 hearts each. */
    private static final Attack QUEEN_LINES = new QueenLines("queen-lines", 4, Hearts.of(2));

    /** Knife throw: 1 heart, then 1 heart of poison at the start of each of the target's next 3 turns. */
    public static final int THROW_DAMAGE = Hearts.of(1);
    public static final int POISON_TURNS = 3;
    public static final int POISON_DAMAGE = Hearts.of(1);

    private static final Attack KNIFE_THROW = new KnifeThrow("knife-throw", THROW_DAMAGE, POISON_TURNS, POISON_DAMAGE);

    private PawnStage stage = PawnStage.ASSASSIN;
    private boolean knifeThrown;

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

    /** Whether the Pawn still has its knife to throw. */
    public boolean hasKnife() {
        return !knifeThrown;
    }

    /**
     * Marks the knife as thrown. It's gone for the rest of the game.
     *
     * @throws IllegalStateException if it was already thrown
     */
    public void throwKnife() {
        if (knifeThrown) {
            throw new IllegalStateException("The Pawn has already thrown its knife");
        }
        knifeThrown = true;
    }

    /** Evolves straight to the final stage, gaining the extra hearts of each stage on the way. */
    public void becomeQueen() {
        while (!stage.isFinal()) {
            evolve();
        }
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
        List<Attack> attacks = new ArrayList<>(switch (stage) {
            case ASSASSIN -> List.of(KNIFE);
            case ENGINEER -> List.of(RICOCHET);
            case QUEEN -> List.of(QUEEN_LINES);
        });
        // The throw is the only way to drop the Keystone, so it stays available until used.
        if (hasKnife() && !stage.isFinal()) {
            attacks.add(KNIFE_THROW);
        }
        return attacks;
    }

    /** The Pawn has no Super: evolving replaces it. */
    @Override
    public Optional<Attack> superAttack() {
        return Optional.empty();
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
