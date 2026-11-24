// The side panels: players, the action buttons and the event log.
//
// Player names are typed by players, so text always goes in with textContent, never
// innerHTML. That way a name like "<img onerror=...>" shows up as text instead of running.

import { SEAT_COLORS, attackName, formatHearts, heroTypeName, pawnStageName } from './heroes.js';

/** Makes an element with an optional class and text. */
export function el(tag, className = '', text = '') {
  const element = document.createElement(tag);
  if (className) element.className = className;
  if (text) element.textContent = text;
  return element;
}

/** One card per hero: hearts, Super meter, poison and the Pawn's stage. */
export class PlayersPanel {
  #root;

  constructor(root) {
    this.#root = root;
  }

  render(view) {
    const cards = view.heroes.map((hero) => this.#card(hero, view));
    this.#root.replaceChildren(...cards);
  }

  #card(hero, view) {
    const card = el('div', 'player');
    if (!hero.alive) card.classList.add('defeated');
    if (!view.over && hero.seat === view.currentSeat) card.classList.add('current');
    card.style.setProperty('--seat', SEAT_COLORS[hero.seat]);

    const title = el('div', 'player-title');
    title.append(el('span', 'swatch'), el('span', 'player-name', hero.name));
    if (hero.seat === view.yourSeat) title.append(el('span', 'tag', 'you'));
    title.append(el('span', 'player-hero', heroTypeName(hero.type)));
    card.append(title);

    if (!hero.alive) {
      card.append(el('div', 'player-line', 'Defeated'));
      return card;
    }

    card.append(meter('Hearts', hero.halfHearts / hero.maxHalfHearts,
      `${formatHearts(hero.halfHearts)} / ${formatHearts(hero.maxHalfHearts)} ♥`, 'hearts'));
    if (hero.hasSuper) {
      const ready = hero.superProgress >= 1;
      card.append(meter('Super', hero.superProgress, ready ? 'READY' : `${Math.round(hero.superProgress * 100)}%`,
        ready ? 'super ready' : 'super'));
    }

    const notes = [];
    if (hero.pawnStage) notes.push(pawnStageName(hero.pawnStage) + (hero.hasKnife ? ' · has knife' : ''));
    if (hero.poisonTurnsLeft > 0) notes.push(`Poisoned (${hero.poisonTurnsLeft} turns)`);
    if (notes.length) card.append(el('div', 'player-line', notes.join(' · ')));
    return card;
  }
}

function meter(label, fraction, text, kind) {
  const row = el('div', `meter ${kind}`);
  const bar = el('div', 'meter-bar');
  const fill = el('div', 'meter-fill');
  fill.style.width = `${Math.max(0, Math.min(1, fraction)) * 100}%`;
  bar.append(fill);
  row.append(el('span', 'meter-label', label), bar, el('span', 'meter-text', text));
  return row;
}

/** Move, one button per available attack, and End turn. */
export class ActionBar {
  #root;
  #handlers;

  /** @param handlers {onMove, onAttack(attackId), onEndTurn} */
  constructor(root, handlers) {
    this.#root = root;
    this.#handlers = handlers;
  }

  /**
   * @param controls the TurnControls
   * @param busy     true while an action is on its way to the server
   */
  render(controls, busy) {
    if (!controls.isMyTurn()) {
      this.#root.replaceChildren();
      return;
    }
    const mode = controls.mode;
    const buttons = [];

    const move = button('Move', 'M', mode.kind === 'move', busy, () => this.#handlers.onMove());
    buttons.push(move);

    controls.attacks().forEach(({ attackId, isSuper }, index) => {
      const label = isSuper ? `★ ${attackName(attackId)}` : attackName(attackId);
      const selected = mode.kind !== 'move' && mode.attackId === attackId;
      const b = button(label, String(index + 1), selected, busy, () => this.#handlers.onAttack(attackId));
      if (isSuper) b.classList.add('super');
      buttons.push(b);
    });

    const end = button('End turn', 'Enter', false, busy, () => this.#handlers.onEndTurn());
    end.classList.add('end-turn');
    buttons.push(end);

    this.#root.replaceChildren(...buttons);
  }
}

function button(label, shortcut, selected, disabled, onClick) {
  const b = el('button', selected ? 'selected' : '');
  b.type = 'button';
  b.disabled = disabled;
  b.append(el('span', '', label), el('kbd', '', shortcut));
  b.addEventListener('click', onClick);
  return b;
}

/** The running list of what happened, newest at the bottom. */
export class EventLog {
  #root;

  constructor(root) {
    this.#root = root;
  }

  add(text, kind = '') {
    const nearBottom = this.#root.scrollHeight - this.#root.scrollTop - this.#root.clientHeight < 40;
    this.#root.append(el('li', kind, text));
    if (nearBottom) this.#root.scrollTop = this.#root.scrollHeight;
  }
}
