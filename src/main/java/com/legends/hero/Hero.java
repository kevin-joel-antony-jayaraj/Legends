package com.legends.hero;

import com.legends.hero.attack.Attack;
import com.legends.hero.meter.SuperMeter;
import com.legends.hero.movement.MovementPattern;
import java.util.List;
import java.util.Optional;

/**
 * A hero on the board. Abstract: you always create a specific hero (Archer, Knight...).
 *
 * <p>What a hero owns: its player, its health and its Super meter. What it does NOT own:
 * its position. The game state is the single source of truth for where pieces are, so
 * the two can never disagree.
 *
 * <p>Subclasses fill in what makes each hero different: its type, its number of actions
 * per turn, how it moves, and its attacks.
 */
public abstract class Hero {

    private final Player owner;
    private final Health health;
    private final SuperMeter superMeter;
    private PoisonStatus poison;

    protected Hero(Player owner, int maxHalfHearts, SuperMeter superMeter) {
        if (owner == null || superMeter == null) {
            throw new IllegalArgumentException("Owner and super meter are required");
        }
        this.owner = owner;
        this.health = new Health(maxHalfHearts);
        this.superMeter = superMeter;
    }

    /** Which kind of hero this is. */
    public abstract HeroType type();

    /** How many actions (move, attack or Super) this hero gets each turn. */
    public abstract int actionsPerTurn();

    /** The rule for where this hero can move. */
    public abstract MovementPattern movement();

    /** The hero's Normal attacks (and abilities like the Knife throw) available right now. */
    public abstract List<Attack> attacks();

    /** The hero's Super, or empty if it has none. Usable only when {@link #isSuperReady()}. */
    public abstract Optional<Attack> superAttack();

    public Player owner() {
        return owner;
    }

    public int halfHearts() {
        return health.current();
    }

    public int maxHalfHearts() {
        return health.max();
    }

    public boolean isAlive() {
        return health.isAlive();
    }

    /**
     * Takes damage and lets the Super meter know (the Knight charges this way).
     *
     * @return how much health was actually lost
     */
    public int takeDamage(int halfHearts) {
        int lost = health.damage(halfHearts);
        if (lost > 0) {
            superMeter.onDamageTaken(lost);
        }
        return lost;
    }

    /** @return how much health was actually restored */
    public int heal(int halfHearts) {
        return health.heal(halfHearts);
    }

    public void healFull() {
        health.healFull();
    }

    /** Called by combat after this hero lands a hit, so the Super meter can charge. */
    public final void recordHitDealt(int halfHearts) {
        superMeter.onHitDealt(halfHearts);
        onHitDealt(halfHearts);
    }

    /**
     * Hook for heroes that react to landing a hit (the Pawn evolves). Does nothing by
     * default. This is the "template method" pattern: the base class decides when it is
     * called, and a subclass only fills in what happens.
     */
    protected void onHitDealt(int halfHearts) {
    }

    public boolean hasSuper() {
        return superMeter.hasSuper();
    }

    public boolean isSuperReady() {
        return superMeter.isFull();
    }

    /** How full the Super meter is, from 0.0 to 1.0. */
    public double superProgress() {
        return superMeter.progress();
    }

    /**
     * Empties the meter when the Super is used.
     *
     * @throws IllegalStateException if the Super isn't ready
     */
    public void consumeSuper() {
        if (!isSuperReady()) {
            throw new IllegalStateException(this + " tried to use its Super before it was charged");
        }
        superMeter.reset();
    }

    // ----- Poison -------------------------------------------------------------------

    public Optional<PoisonStatus> poison() {
        return Optional.ofNullable(poison);
    }

    /**
     * Poisons the hero. Poison doesn't stack: if already poisoned, the hero keeps
     * whichever poison has more turns left.
     */
    public void applyPoison(PoisonStatus newPoison) {
        if (!isAlive()) {
            return;
        }
        if (poison == null || newPoison.turnsLeft() > poison.turnsLeft()) {
            poison = newPoison;
        }
    }

    /**
     * Uses up one turn of poison. The caller deals the damage, so it can report who did it.
     *
     * @return the poison that ticked this turn, or empty if the hero isn't poisoned
     */
    public Optional<PoisonStatus> tickPoison() {
        PoisonStatus ticking = poison;
        if (ticking != null) {
            poison = ticking.afterOneTurn();
        }
        return Optional.ofNullable(ticking);
    }

    /** For heroes that grow stronger (the Pawn). */
    protected void raiseMaxHealth(int newMaxHalfHearts) {
        health.raiseMax(newMaxHalfHearts);
    }

    @Override
    public String toString() {
        return type() + "(" + owner.name() + ", " + Hearts.format(halfHearts()) + "/"
                + Hearts.format(maxHalfHearts()) + " hearts)";
    }
}
