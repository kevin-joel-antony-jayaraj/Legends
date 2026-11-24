// The game page. Wires the pieces together:
//
//   GameClient    REST: load my view, send actions
//   StompClient   WebSocket: hear about everyone's actions as they happen
//   TurnControls  what my clicks mean (only picks from options the server sent)
//   BoardRenderer draws the board
//   panels        players, buttons, event log
//
// The server is the only source of truth. After any event, the page asks for a fresh
// view instead of trying to work out the new state itself.

import { GameClient } from './api.js';
import { BoardRenderer } from './board-renderer.js';
import { describeEvent, eventKind } from './event-text.js';
import { heroLabel } from './heroes.js';
import { ActionBar, EventLog, PlayersPanel } from './panels.js';
import { same } from './squares.js';
import { StompClient } from './stomp.js';
import { TurnControls } from './turn-controls.js';

const params = new URLSearchParams(window.location.search);
const gameId = params.get('game');
const token = params.get('token');

const $ = (id) => document.getElementById(id);
const canvas = $('board');
const statusLine = $('status');
const hintLine = $('hint');
const connection = $('connection');
const toast = $('toast');

const client = new GameClient(gameId, token);
const controls = new TurnControls();
const renderer = new BoardRenderer(canvas);
const players = new PlayersPanel($('players'));
const log = new EventLog($('log'));
const actionBar = new ActionBar($('actions'), {
  onMove: () => { controls.selectMove(); render(); },
  onAttack: (attackId) => { controls.selectAttack(attackId); render(); },
  onEndTurn: () => perform({ type: 'END_TURN' }),
});

let view = null;
let hover = null;
let busy = false;
let latestRequest = 0; // so a slow, older response can't overwrite a newer one
let gameOverClosed = false;

// ----- loading and acting ----------------------------------------------------------

async function refresh() {
  const request = ++latestRequest;
  try {
    const next = await client.view();
    if (request === latestRequest) show(next);
  } catch (error) {
    showError(error.message);
  }
}

async function perform(action) {
  if (busy) return;
  busy = true;
  render();
  const request = ++latestRequest;
  try {
    const outcome = await client.act(action);
    controls.selectMove();
    if (request === latestRequest) show(outcome.view);
  } catch (error) {
    showError(error.message);
  } finally {
    busy = false;
    render();
  }
}

function show(next) {
  view = next;
  controls.update(view);
  render();
}

// ----- drawing ---------------------------------------------------------------------

function render() {
  if (!view) return;
  renderer.draw(view, controls.highlights(hover));
  players.render(view);
  actionBar.render(controls, busy);
  hintLine.textContent = controls.hint();

  const you = view.heroes.find((h) => h.seat === view.yourSeat);
  const current = view.heroes.find((h) => h.seat === view.currentSeat);
  if (view.over) {
    const winner = view.heroes.find((h) => h.seat === view.winnerSeat);
    statusLine.textContent = winner ? `${heroLabel(winner)} wins!` : 'Game over: nobody survived.';
    document.title = 'Legends: game over';
  } else if (controls.isMyTurn()) {
    const n = view.actionsLeft;
    statusLine.textContent = `Your turn: ${n} action${n === 1 ? '' : 's'} left`;
    document.title = '▶ Your turn | Legends';
  } else if (!you.alive) {
    statusLine.textContent = `You're out. Watching ${heroLabel(current)}…`;
    document.title = 'Legends';
  } else {
    statusLine.textContent = `Waiting for ${heroLabel(current)}…`;
    document.title = 'Legends';
  }
  statusLine.classList.toggle('your-turn', controls.isMyTurn());
  $('gameover').hidden = !view.over || gameOverClosed;
  if (view.over) $('gameover-text').textContent = statusLine.textContent;
}

let toastTimer = null;
function showError(message) {
  toast.textContent = message;
  toast.hidden = false;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { toast.hidden = true; }, 4000);
}

// ----- input -----------------------------------------------------------------------

$('gameover-close').addEventListener('click', () => {
  gameOverClosed = true;
  render();
});

canvas.addEventListener('click', (event) => {
  if (!view || busy) return;
  const action = controls.click(renderer.squareAt(event.clientX, event.clientY));
  if (action) perform(action);
  else render();
});

canvas.addEventListener('mousemove', (event) => {
  if (!view) return;
  const square = renderer.squareAt(event.clientX, event.clientY);
  if (!same(square, hover) && !(square == null && hover == null)) {
    hover = square;
    render();
  }
});

canvas.addEventListener('mouseleave', () => {
  hover = null;
  if (view) render();
});

document.addEventListener('keydown', (event) => {
  if (!view || busy || event.target instanceof HTMLInputElement) return;
  if (event.key === 'Escape') controls.cancel();
  else if (event.key === 'm' || event.key === 'M') controls.selectMove();
  else if (event.key === 'Enter' && controls.isMyTurn()) return void perform({ type: 'END_TURN' });
  else if (/^[1-9]$/.test(event.key)) {
    const attack = controls.attacks()[Number(event.key) - 1];
    if (attack) controls.selectAttack(attack.attackId);
  } else return;
  render();
});

// ----- live updates ----------------------------------------------------------------

function start() {
  if (!gameId || !token) {
    $('missing').hidden = false;
    $('game').hidden = true;
    return;
  }

  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
  const live = new StompClient(`${protocol}://${window.location.host}/ws`, {
    onStatus: (status) => {
      connection.textContent = { connecting: 'connecting…', live: '● live', offline: 'reconnecting…' }[status];
      connection.className = status;
      if (status === 'live') refresh(); // catch up on anything missed while offline
    },
  });
  live.subscribe(`/topic/games/${gameId}`, (events) => {
    for (const event of events) log.add(describeEvent(event, view?.heroes), eventKind(event));
    refresh();
  });
  live.connect();
  refresh();
}

start();
