package com.legends.game;

import com.legends.board.Board;
import com.legends.board.BoardView;
import com.legends.hero.Hero;
import com.legends.hero.attack.Attack;
import com.legends.hero.attack.AttackOption;
import com.legends.model.Position;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Everything about a game in progress: the board, the heroes, where they stand, and
 * whose turn it is.
 *
 * <p>This is the single source of truth for positions. Heroes don't store their own
 * position, so there is never a second copy to fall out of sync.
 *
 * <p>Every change is checked before it happens. A request that breaks the rules (moving
 * onto an occupied square, spending an action you don't have) throws an exception
 * instead of leaving the game in a broken state. That matters once real players send
 * requests over the network.
 */
public final class GameState implements BoardView {

    private final Board board;
    private final long seed;
    private final List<Hero> seats;
    private final Map<Hero, Position> positions = new HashMap<>();
    private final Map<Position, Hero> occupants = new HashMap<>();
    private final List<Bomb> bombs = new ArrayList<>();
    private Keystone keystone;

    private int currentSeat;
    private int actionsLeft;

    /**
     * Created by {@link GameSetup}. Package-private so games are always built the proper way.
     *
     * @param seats     heroes in clockwise turn order
     * @param starts    start square for each hero, same order as {@code seats}
     * @param firstSeat index into {@code seats} of the player who goes first
     */
    GameState(Board board, long seed, List<Hero> seats, List<Position> starts, int firstSeat) {
        if (seats.size() != starts.size()) {
            throw new IllegalArgumentException("Need one start square per hero");
        }
        if (firstSeat < 0 || firstSeat >= seats.size()) {
            throw new IllegalArgumentException("First seat " + firstSeat + " is out of range");
        }
        this.board = board;
        this.seed = seed;
        this.seats = List.copyOf(seats);
        for (int i = 0; i < seats.size(); i++) {
            place(seats.get(i), starts.get(i));
        }
        this.currentSeat = firstSeat;
        this.actionsLeft = currentHero().actionsPerTurn();
    }

    // ----- Board and positions -------------------------------------------------------

    @Override
    public Board board() {
        return board;
    }

    /** The seed the board was generated from. Store it to replay or debug a game. */
    public long seed() {
        return seed;
    }

    @Override
    public boolean isOccupied(Position p) {
        return occupants.containsKey(p);
    }

    public Optional<Hero> heroAt(Position p) {
        return Optional.ofNullable(occupants.get(p));
    }

    /** Where a hero stands. Empty if the hero has been removed from the board. */
    public Optional<Position> positionOf(Hero hero) {
        return Optional.ofNullable(positions.get(hero));
    }

    /** All heroes in clockwise seat order, alive or not. */
    public List<Hero> heroes() {
        return seats;
    }

    public List<Hero> livingHeroes() {
        return seats.stream().filter(Hero::isAlive).toList();
    }

    /**
     * The hero in a seat.
     *
     * @throws IllegalArgumentException if there is no such seat
     */
    public Hero heroInSeat(int seat) {
        if (seat < 0 || seat >= seats.size()) {
            throw new IllegalArgumentException("No player in seat " + seat);
        }
        return seats.get(seat);
    }

    // ----- Movement -----------------------------------------------------------------

    /** Every square this hero could move to right now. Empty for dead or removed heroes. */
    public Set<Position> legalMoves(Hero hero) {
        Position from = positions.get(hero);
        if (from == null || !hero.isAlive()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(hero.movement().destinations(from, this));
    }

    /**
     * Moves a hero. Only checks that the move is legal for the hero's movement pattern;
     * turn rules (whose turn it is, actions left) are checked by the action layer.
     *
     * @throws IllegalArgumentException if the destination isn't a legal move
     */
    public void moveHero(Hero hero, Position destination) {
        if (!legalMoves(hero).contains(destination)) {
            throw new IllegalArgumentException(hero + " can't move to " + destination);
        }
        Position from = positions.get(hero);
        occupants.remove(from);
        occupants.put(destination, hero);
        positions.put(hero, destination);
    }

    /**
     * Puts a hero on a square without following its movement pattern. For effects like
     * the Knight's Check (and later pushes, pulls and teleports) where the rules decide the
     * destination. Still refuses squares that are off the board, unwalkable or occupied.
     *
     * @throws IllegalArgumentException if the hero isn't on the board or the square isn't free
     */
    public void relocateHero(Hero hero, Position destination) {
        Position from = positions.get(hero);
        if (from == null) {
            throw new IllegalArgumentException(hero + " is not on the board");
        }
        if (from.equals(destination)) {
            return;
        }
        if (!isFreeToEnter(destination)) {
            throw new IllegalArgumentException(hero + " can't be placed on " + destination);
        }
        occupants.remove(from);
        occupants.put(destination, hero);
        positions.put(hero, destination);
    }

    // ----- Attacks ------------------------------------------------------------------

    /**
     * Every attack this hero could make right now, including its Super if charged.
     * Empty for dead or removed heroes.
     */
    public List<AttackOption> attackOptions(Hero hero) {
        Position from = positions.get(hero);
        if (from == null || !hero.isAlive()) {
            return List.of();
        }
        List<AttackOption> options = new ArrayList<>();
        for (Attack attack : hero.attacks()) {
            options.addAll(attack.options(from, this));
        }
        if (hero.isSuperReady()) {
            hero.superAttack().ifPresent(attack -> options.addAll(attack.options(from, this)));
        }
        return options;
    }

    /** True if {@code attackId} is this hero's Super. */
    public boolean isSuper(Hero hero, String attackId) {
        return hero.superAttack().map(attack -> attack.id().equals(attackId)).orElse(false);
    }

    /** True if the hero could move or attack right now. */
    public boolean hasAnyLegalAction(Hero hero) {
        return !legalMoves(hero).isEmpty() || !attackOptions(hero).isEmpty();
    }

    /**
     * Takes every hero at 0 hearts off the board, freeing their squares.
     *
     * @return the heroes removed by this call
     */
    public List<Hero> removeDeadHeroes() {
        List<Hero> removed = new ArrayList<>();
        for (Hero hero : seats) {
            if (!hero.isAlive() && positions.containsKey(hero)) {
                occupants.remove(positions.remove(hero));
                removed.add(hero);
            }
        }
        return removed;
    }

    // ----- Bombs (hidden) and the Keystone -------------------------------------------

    /**
     * Hides a bomb. More than one bomb may share a square.
     *
     * @throws IllegalArgumentException if the square is off the board or not walkable
     */
    public void placeBomb(Position at, int ownerSeat) {
        if (!board.contains(at) || !board.tileAt(at).isWalkable()) {
            throw new IllegalArgumentException("Can't place a bomb on " + at);
        }
        bombs.add(new Bomb(at, ownerSeat));
    }

    /**
     * All hidden bombs. For the rules engine only: this is secret information that must
     * never be shown or sent to players.
     */
    public List<Bomb> hiddenBombs() {
        return List.copyOf(bombs);
    }

    /** @return true if the bomb was there and is now gone */
    public boolean removeBomb(Bomb bomb) {
        return bombs.remove(bomb);
    }

    public Optional<Keystone> keystone() {
        return Optional.ofNullable(keystone);
    }

    /**
     * Puts the Keystone on the board. There is only ever one.
     *
     * @throws IllegalStateException if a Keystone is already on the board
     */
    public void dropKeystone(Position at, int ownerSeat) {
        if (keystone != null) {
            throw new IllegalStateException("There is already a Keystone on " + keystone.at());
        }
        keystone = new Keystone(at, ownerSeat);
    }

    public void removeKeystone() {
        keystone = null;
    }

    // ----- Turns --------------------------------------------------------------------

    public int currentSeat() {
        return currentSeat;
    }

    public Hero currentHero() {
        return seats.get(currentSeat);
    }

    public int actionsLeft() {
        return actionsLeft;
    }

    /**
     * Uses up one of the current hero's actions.
     *
     * @throws IllegalStateException if none are left
     */
    public void spendAction() {
        if (actionsLeft <= 0) {
            throw new IllegalStateException(currentHero() + " has no actions left this turn");
        }
        actionsLeft--;
    }

    /** Passes the turn clockwise to the next living hero, who gets a fresh set of actions. */
    public void endTurn() {
        if (isOver()) {
            return;
        }
        do {
            currentSeat = (currentSeat + 1) % seats.size();
        } while (!seats.get(currentSeat).isAlive());
        actionsLeft = currentHero().actionsPerTurn();
    }

    /** The game ends when one hero (or none) is left standing. */
    public boolean isOver() {
        return livingHeroes().size() <= 1;
    }

    /** The last hero standing, once the game is over. */
    public Optional<Hero> winner() {
        List<Hero> living = livingHeroes();
        return living.size() == 1 ? Optional.of(living.get(0)) : Optional.empty();
    }

    private void place(Hero hero, Position p) {
        if (!isFreeToEnter(p)) {
            throw new IllegalArgumentException("Can't place " + hero + " on " + p);
        }
        if (positions.containsKey(hero)) {
            throw new IllegalArgumentException(hero + " is already on the board");
        }
        positions.put(hero, p);
        occupants.put(p, hero);
    }
}
