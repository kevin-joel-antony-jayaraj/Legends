// The new-game page: pick players and heroes, create the game, then hand out one join
// link per seat. A real lobby where players join on their own comes later.

import { createGame, serverInfo } from './api.js';
import { HERO_TYPES, SEAT_COLORS, heroTypeName } from './heroes.js';
import { el } from './panels.js';

const MIN_PLAYERS = 2;
const MAX_PLAYERS = 4;

const $ = (id) => document.getElementById(id);
const rows = $('player-rows');
const toast = $('toast');

// ----- setup form ------------------------------------------------------------------

function addRow() {
  const seat = rows.children.length;
  const row = el('div', 'player-row');
  row.style.setProperty('--seat', SEAT_COLORS[seat]);

  const name = el('input');
  name.required = true;
  name.maxLength = 20;
  name.value = `Player ${seat + 1}`;
  name.setAttribute('aria-label', `Player ${seat + 1} name`);

  const hero = el('select');
  hero.setAttribute('aria-label', `Player ${seat + 1} hero`);
  for (const type of HERO_TYPES) {
    const option = el('option', '', heroTypeName(type));
    option.value = type;
    hero.append(option);
  }
  hero.value = HERO_TYPES[seat % HERO_TYPES.length];

  row.append(el('span', 'swatch'), name, hero);
  rows.append(row);
  updateRowButtons();
}

function removeRow() {
  rows.lastElementChild?.remove();
  updateRowButtons();
}

function updateRowButtons() {
  $('add-player').disabled = rows.children.length >= MAX_PLAYERS;
  $('remove-player').disabled = rows.children.length <= MIN_PLAYERS;
}

function readRequest() {
  const players = [...rows.children].map((row) => ({
    name: row.querySelector('input').value.trim(),
    hero: row.querySelector('select').value,
  }));
  const seedText = $('seed').value.trim();
  if (seedText === '') return { players };
  const seed = Number(seedText);
  // Bigger numbers get rounded by JavaScript, which would quietly give a different board.
  if (!Number.isSafeInteger(seed)) throw new Error(`The seed must be a whole number up to ${Number.MAX_SAFE_INTEGER}`);
  return { seed, players };
}

// ----- join links ------------------------------------------------------------------

let created = null;

/** Hosts other computers can use, best first. "localhost" only works on this computer. */
async function joinHosts() {
  const here = window.location.host;
  const hosts = [];
  try {
    const info = await serverInfo();
    for (const address of info.lanAddresses) hosts.push(`${address}:${info.port}`);
  } catch {
    // Fall back to whatever address this page was opened with.
  }
  if (!hosts.includes(here)) hosts.push(here);
  return hosts;
}

function linkFor(host, seat) {
  const query = new URLSearchParams({ game: created.gameId, token: seat.token });
  return `${window.location.protocol}//${host}/play.html?${query}`;
}

function renderLinks() {
  const host = $('address').value;
  const local = /^(localhost|127\.0\.0\.1)(:|$)/.test(host);
  $('address-note').textContent = local
    ? 'These links only work on this computer. Pick a network address for other players.'
    : 'Other computers must be on the same network. If your firewall asks, allow Java to accept connections.';

  const items = created.seats.map((seat) => {
    const url = linkFor(host, seat);
    const item = el('li', 'seat-link');
    item.style.setProperty('--seat', SEAT_COLORS[seat.seat]);

    const who = el('div', 'seat-who');
    who.append(el('span', 'swatch'), el('strong', '', seat.name), el('span', 'muted', ` ${heroTypeName(seat.hero)}`));

    const field = el('input');
    field.readOnly = true;
    field.value = url;
    field.addEventListener('focus', () => field.select());

    const copy = el('button', '', 'Copy');
    copy.type = 'button';
    copy.addEventListener('click', () => copyText(field, copy));

    const open = el('a', 'button', 'Open');
    open.href = url;
    open.target = '_blank';
    open.rel = 'noopener';

    const actions = el('div', 'seat-actions');
    actions.append(field, copy, open);
    item.append(who, actions);
    return item;
  });
  $('seat-links').replaceChildren(...items);
}

async function copyText(field, button) {
  try {
    await navigator.clipboard.writeText(field.value);
  } catch {
    // The clipboard API needs https or localhost; fall back to the old way.
    field.select();
    document.execCommand('copy');
  }
  button.textContent = 'Copied';
  setTimeout(() => { button.textContent = 'Copy'; }, 1500);
}

// ----- wiring ----------------------------------------------------------------------

$('add-player').addEventListener('click', addRow);
$('remove-player').addEventListener('click', removeRow);

$('setup-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const submit = event.submitter;
  submit.disabled = true;
  try {
    created = await createGame(readRequest());
    const hosts = await joinHosts();
    $('address').replaceChildren(...hosts.map((host) => {
      const option = el('option', '', host);
      option.value = host;
      return option;
    }));
    $('seed-used').textContent = String(created.seed);
    renderLinks();
    $('setup').hidden = true;
    $('links').hidden = false;
  } catch (error) {
    toast.textContent = error.message;
    toast.hidden = false;
    setTimeout(() => { toast.hidden = true; }, 4000);
  } finally {
    submit.disabled = false;
  }
});

$('address').addEventListener('change', renderLinks);
$('again').addEventListener('click', () => {
  $('links').hidden = true;
  $('setup').hidden = false;
});

for (let i = 0; i < MAX_PLAYERS; i++) addRow();
