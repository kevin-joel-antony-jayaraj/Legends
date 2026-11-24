package com.legends.hero;

import com.legends.hero.meter.SuperMeter;
import com.legends.hero.movement.MovementPattern;

/**
 * A hero on the board. Abstract: you always create a specific hero (Archer, Knight...).
 *
 * <p>What a hero owns: its player, its health and its Super meter. What it does NOT own:
 * its position. The game state is the single source of truth for where pieces are, so
 * the two can never disagree.
 *
 * <p>Subclasses fill in what makes each hero different: its type, its number of actions
 * per turn, and how it moves.
 */
public abstract class Hero {

    private final Player owner;
    private final Health health;
    private final SuperMeter superMeter;

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
    public void recordHitDealt(int halfHearts) {
        superMeter.onHitDealt(halfHearts);
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
