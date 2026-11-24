// Turns the server's game events into sentences for the log.
// Mirrors com.legends.cli.EventText; the event names and fields come from GameEvent.java.

import { attackName, formatHearts, heroLabel, pawnStageName } from './heroes.js';

const NO_WINNER = -1;

/**
 * @param event  {type, data} as sent by the server
 * @param heroes the heroes from the latest view, to turn seats into names
 */
export function describeEvent(event, heroes) {
  const name = (seat) => {
    const hero = (heroes ?? []).find((h) => h.seat === seat);
    return hero ? heroLabel(hero) : `Seat ${seat + 1}`;
  };
  const square = (p) => `(${p.row},${p.col})`;
  const d = event.data;

  switch (event.type) {
    case 'HeroMoved': return `${name(d.seat)} moves ${square(d.from)} → ${square(d.to)}`;
    case 'HeroDamaged': return `${name(d.targetSeat)} takes ${formatHearts(d.halfHearts)} ♥ from `
        + `${name(d.attackerSeat)}'s ${attackName(d.attackId)} (${formatHearts(d.halfHeartsLeft)} left)`;
    case 'HeroHealed': return `${name(d.seat)} is healed for ${formatHearts(d.halfHearts)} ♥`;
    case 'HeroPoisoned': return `${name(d.seat)} is poisoned for ${d.turns} turns`;
    case 'HeroDefeated': return `${name(d.seat)} is defeated!`;
    case 'SuperUsed': return `${name(d.seat)} uses their Super: ${attackName(d.attackId)}!`;
    case 'PawnEvolved': return `${name(d.seat)} evolves into ${pawnStageName(d.stage)}!`;
    case 'BombPlaced': return `${name(d.seat)} hides a bomb somewhere…`;
    case 'BombExploded': return `BOOM! A bomb explodes at ${square(d.at)}`;
    case 'KeystoneDropped': return `The Keystone appears at ${square(d.at)}`;
    case 'KeystoneClaimed': return `${name(d.seat)} claims the Keystone!`;
    case 'TurnStarted': return `${name(d.seat)}'s turn (${d.actions} actions)`;
    case 'TurnSkipped': return `${name(d.seat)} has no legal action and is skipped`;
    case 'GameOver': return d.winnerSeat === NO_WINNER ? 'Game over: nobody survived.' : `Game over: ${name(d.winnerSeat)} wins!`;
    default: return event.type;
  }
}

/** A style hint for the log: "turn", "danger", "super" or "" */
export function eventKind(event) {
  switch (event.type) {
    case 'TurnStarted': case 'TurnSkipped': return 'turn';
    case 'HeroDamaged': case 'HeroDefeated': case 'BombExploded': case 'HeroPoisoned': return 'danger';
    case 'SuperUsed': case 'PawnEvolved': case 'KeystoneClaimed': case 'GameOver': return 'super';
    default: return '';
  }
}
