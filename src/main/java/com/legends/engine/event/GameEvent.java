package com.legends.engine.event;

import com.legends.hero.PawnStage;
import com.legends.model.Position;

/**
 * Something that happened in the game, reported after each action.
 *
 * <p>Events describe changes; they don't cause them. The console prints them now, and
 * later the server will send them to each browser so it can animate what happened.
 *
 * <p>The interface is "sealed": these records are the only kinds of event, so code that
 * handles events with a {@code switch} is checked by the compiler to cover every one.
 */
public sealed interface GameEvent {

    /** A hero moved, by walking, jumping, or as part of an attack. */
    record HeroMoved(int seat, Position from, Position to) implements GameEvent {
    }

    /** A hero lost health. {@code halfHearts} is what was actually lost. */
    record HeroDamaged(int targetSeat, int attackerSeat, String attackId, int halfHearts, int halfHeartsLeft)
            implements GameEvent {
    }

    /** A hero reached 0 hearts and was taken off the board. */
    record HeroDefeated(int seat) implements GameEvent {
    }

    /** The Pawn reached a new stage. */
    record PawnEvolved(int seat, PawnStage stage) implements GameEvent {
    }

    /** A new turn began. */
    record TurnStarted(int seat, int actions) implements GameEvent {
    }

    /** A hero's turn was skipped because it had no legal action. */
    record TurnSkipped(int seat) implements GameEvent {
    }

    /** The game ended. {@code winnerSeat} is {@link #NO_WINNER} if nobody survived. */
    record GameOver(int winnerSeat) implements GameEvent {
        public static final int NO_WINNER = -1;
    }
}
