// What a click on the board means right now, and what to highlight.
//
// This holds no game rules. The server sends the legal moves and every legal attack
// option (view.yourMoves, view.yourAttacks); this class only lets the player pick one
// of them, then hands back the exact action to send. It never touches the page, so it
// can be tested on its own (src/test/js/turn-controls.test.js).
//
// Modes:
//   move    click a highlighted square to walk there (the default)
//   attack  an attack is picked; click one of its aim squares. Attacks with only one
//           option (Push Back, Pull, Queen lines: they hit everything around the hero)
//           also fire on a click on anything they affect, or on picking them again
//   choose  that aim can be done more than one way (the Knight's Check can land or
//           push differently), so click where to land, then where to push

import { formatHearts, attackName } from './heroes.js';
import { same, unique } from './squares.js';

const MOVE = { kind: 'move' };

export class TurnControls {
  #view = null;
  #mode = MOVE;

  /** Call with every new view. Drops a picked attack if it's no longer possible. */
  update(view) {
    this.#view = view;
    if (!this.isMyTurn()) {
      this.#mode = MOVE;
    } else if (this.#mode.kind !== 'move' && !this.attacks().some((a) => a.attackId === this.#mode.attackId)) {
      this.#mode = MOVE;
    } else if (this.#mode.kind === 'choose') {
      this.#mode = { kind: 'attack', attackId: this.#mode.attackId }; // its choices may be out of date
    }
  }

  isMyTurn() {
    const view = this.#view;
    return view != null && !view.over && view.currentSeat === view.yourSeat;
  }

  /** {kind, attackId?}: which mode the player is in. */
  get mode() {
    return { kind: this.#mode.kind, attackId: this.#mode.attackId };
  }

  /** The attacks that can be used right now, once each, in the server's order: [{attackId, isSuper}]. */
  attacks() {
    const byId = new Map();
    for (const choice of this.#view?.yourAttacks ?? []) {
      if (!byId.has(choice.attackId)) byId.set(choice.attackId, { attackId: choice.attackId, isSuper: choice.isSuper });
    }
    return [...byId.values()];
  }

  selectMove() {
    this.#mode = MOVE;
  }

  selectAttack(attackId) {
    if (this.attacks().some((a) => a.attackId === attackId)) {
      this.#mode = { kind: 'attack', attackId };
    }
  }

  /** Escape: steps back from choosing to the attack, or from the attack to moving. */
  cancel() {
    this.#mode = this.#mode.kind === 'choose' ? { kind: 'attack', attackId: this.#mode.attackId } : MOVE;
  }

  /**
   * Handles a click on a square.
   *
   * @returns the action to send to the server, or null if the click only changed the
   *          selection (or did nothing)
   */
  click(square) {
    if (!this.isMyTurn() || square == null) return null;
    const mode = this.#mode;

    if (mode.kind === 'move') {
      return this.#view.yourMoves.some((move) => same(move, square)) ? { type: 'MOVE', to: square } : null;
    }

    if (mode.kind === 'attack') {
      const choices = this.#choicesAimedAt(mode.attackId, square);
      if (choices.length === 0) {
        const only = this.#onlyChoice(mode.attackId);
        return only && affects(only, square) ? attackAction(only) : null;
      }
      if (choices.length === 1) return attackAction(choices[0]);
      this.#mode = { kind: 'choose', attackId: mode.attackId, aim: square, choices };
      return null;
    }

    // choose
    const field = distinguishingField(mode.choices);
    const narrowed = mode.choices.filter((choice) => same(choice[field], square));
    if (narrowed.length === 0) {
      this.#mode = { kind: 'attack', attackId: mode.attackId }; // clicked elsewhere: back out
      return null;
    }
    if (narrowed.length === 1) return attackAction(narrowed[0]);
    this.#mode = { ...mode, choices: narrowed };
    return null;
  }

  /**
   * Picking an attack that's already picked: fires it if there's nothing to choose
   * (it has a single option), otherwise does nothing.
   *
   * @returns the action to send, or null
   */
  confirm() {
    const mode = this.#mode;
    if (!this.isMyTurn() || mode.kind !== 'attack') return null;
    const only = this.#onlyChoice(mode.attackId);
    return only ? attackAction(only) : null;
  }

  /** One line telling the player what to do next. */
  hint() {
    if (!this.isMyTurn()) return '';
    const mode = this.#mode;
    if (mode.kind === 'move') {
      return this.attacks().length > 0
        ? 'Click a dotted square to move, or pick an attack.'
        : 'Click a dotted square to move, or end your turn.';
    }
    const name = attackName(mode.attackId);
    if (mode.kind === 'attack') {
      const only = this.#onlyChoice(mode.attackId);
      if (only && same(only.aim, this.#yourPosition())) {
        return `${name}: click your hero, anyone it affects, or ${name} again to use it.`;
      }
      return `Click an orange square to aim ${name}. Hover first to preview it.`;
    }
    return distinguishingField(mode.choices) === 'landing'
      ? `${name} can end in more than one place: click where to land.`
      : `${name} can push more than one way: click where the target goes.`;
  }

  /**
   * What to draw for the current mode, given the square under the mouse.
   *
   * @returns {moves, aims, picks, hits: [{at, label}], pushes: [{from, to}], landing, selected}
   */
  highlights(hover) {
    const none = { moves: [], aims: [], picks: [], hits: [], pushes: [], landing: null, selected: null };
    if (!this.isMyTurn()) return none;
    const mode = this.#mode;

    if (mode.kind === 'move') {
      return { ...none, moves: this.#view.yourMoves };
    }

    if (mode.kind === 'attack') {
      const choices = this.#choicesFor(mode.attackId);
      const aims = unique(choices.map((c) => c.aim));
      // Preview the hovered aim; if there's only one aim, always preview it.
      const preview = (hover && choices.find((c) => same(c.aim, hover))) ?? (aims.length === 1 ? choices[0] : null);
      return { ...none, aims, ...this.#preview(preview) };
    }

    const field = distinguishingField(mode.choices);
    const preview = hover ? mode.choices.find((c) => same(c[field], hover)) : null;
    return { ...none, picks: unique(mode.choices.map((c) => c[field])), selected: mode.aim, ...this.#preview(preview) };
  }

  #preview(choice) {
    if (!choice) return {};
    const landing = same(choice.landing, this.#yourPosition()) ? null : choice.landing;
    return {
      hits: choice.hits.map((hit) => ({ at: hit.target, label: `-${formatHearts(hit.halfHearts)}` })),
      pushes: choice.pushes ?? [],
      landing,
    };
  }

  /** The attack's option if it has exactly one, else null. */
  #onlyChoice(attackId) {
    const choices = this.#choicesFor(attackId);
    return choices.length === 1 ? choices[0] : null;
  }

  #choicesFor(attackId) {
    return (this.#view?.yourAttacks ?? []).filter((c) => c.attackId === attackId);
  }

  #choicesAimedAt(attackId, square) {
    return this.#choicesFor(attackId).filter((c) => same(c.aim, square));
  }

  #yourPosition() {
    return this.#view?.heroes.find((h) => h.seat === this.#view.yourSeat)?.position ?? null;
  }
}

/** The request that picks exactly this option; the server accepts it only if it's legal. */
function attackAction(choice) {
  return { type: 'ATTACK', attackId: choice.attackId, aim: choice.aim, landing: choice.landing, pushTo: choice.pushTo };
}

/** True if the option aims at, damages, or moves anything on this square. */
function affects(choice, square) {
  return same(choice.aim, square)
    || choice.hits.some((hit) => same(hit.target, square))
    || (choice.pushes ?? []).some((push) => same(push.from, square) || same(push.to, square));
}

/** Which field tells these options apart: where the attacker lands, or else where the target is pushed. */
function distinguishingField(choices) {
  return unique(choices.map((c) => c.landing)).length > 1 ? 'landing' : 'pushTo';
}
