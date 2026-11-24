// Tests for the board click logic. Run with: node --test src/test/js/
import assert from 'node:assert/strict';
import { test } from 'node:test';
import { TurnControls } from '../../main/resources/static/js/turn-controls.js';

const at = (row, col) => ({ row, col });

/** A view shaped like the server's GameView, with only the fields TurnControls reads. */
function view({ yourSeat = 0, currentSeat = 0, over = false, moves = [], attacks = [] } = {}) {
  return {
    yourSeat,
    currentSeat,
    over,
    heroes: [{ seat: 0, position: at(5, 5) }, { seat: 1, position: at(3, 5) }],
    yourMoves: moves,
    yourAttacks: attacks,
  };
}

function choice(attackId, aim, { landing = at(5, 5), pushTo = null, isSuper = false, hits = [] } = {}) {
  return { attackId, isSuper, aim, landing, pushTo, hits };
}

test('clicking a legal move sends a MOVE', () => {
  const controls = new TurnControls();
  controls.update(view({ moves: [at(5, 6)] }));
  assert.deepEqual(controls.click(at(5, 6)), { type: 'MOVE', to: at(5, 6) });
  assert.equal(controls.click(at(9, 9)), null);
});

test("nothing happens when it isn't your turn", () => {
  const controls = new TurnControls();
  controls.update(view({ currentSeat: 1, moves: [at(5, 6)] }));
  assert.equal(controls.click(at(5, 6)), null);
  assert.deepEqual(controls.highlights(null).moves, []);
});

test('an attack with one way to hit the square is sent straight away, exactly as offered', () => {
  const shot = choice('diagonal-shot', at(3, 3), { hits: [{ target: at(3, 3), halfHearts: 4 }] });
  const controls = new TurnControls();
  controls.update(view({ attacks: [shot] }));
  controls.selectAttack('diagonal-shot');
  assert.deepEqual(controls.click(at(3, 3)), {
    type: 'ATTACK', attackId: 'diagonal-shot', aim: at(3, 3), landing: at(5, 5), pushTo: null,
  });
});

test('hovering an aim previews its damage', () => {
  const shot = choice('diagonal-shot', at(3, 3), { hits: [{ target: at(3, 3), halfHearts: 3 }] });
  const controls = new TurnControls();
  controls.update(view({ attacks: [shot] }));
  controls.selectAttack('diagonal-shot');
  assert.deepEqual(controls.highlights(at(3, 3)).hits, [{ at: at(3, 3), label: '-1.5' }]);
});

test('a Check that can land two ways asks where to land, then sends that one', () => {
  const landHere = choice('check', at(3, 5), { landing: at(3, 5), pushTo: at(2, 5) });
  const landShort = choice('check', at(3, 5), { landing: at(4, 5) });
  const controls = new TurnControls();
  controls.update(view({ attacks: [landHere, landShort] }));
  controls.selectAttack('check');

  assert.equal(controls.click(at(3, 5)), null, 'needs a second click');
  assert.equal(controls.mode.kind, 'choose');
  assert.deepEqual(controls.highlights(null).picks, [at(3, 5), at(4, 5)]);

  assert.deepEqual(controls.click(at(4, 5)), {
    type: 'ATTACK', attackId: 'check', aim: at(3, 5), landing: at(4, 5), pushTo: null,
  });
});

test('clicking away while choosing backs out to the attack', () => {
  const a = choice('check', at(3, 5), { landing: at(3, 5) });
  const b = choice('check', at(3, 5), { landing: at(4, 5) });
  const controls = new TurnControls();
  controls.update(view({ attacks: [a, b] }));
  controls.selectAttack('check');
  controls.click(at(3, 5));
  assert.equal(controls.click(at(0, 0)), null);
  assert.deepEqual(controls.mode, { kind: 'attack', attackId: 'check' });
});

test('a picked attack is dropped when the new view no longer offers it', () => {
  const controls = new TurnControls();
  controls.update(view({ attacks: [choice('push-back', at(5, 5), { isSuper: true })] }));
  controls.selectAttack('push-back');
  controls.update(view({ attacks: [] }));
  assert.equal(controls.mode.kind, 'move');
});

test('attacks are listed once each, in order', () => {
  const controls = new TurnControls();
  controls.update(view({ attacks: [
    choice('melee', at(4, 5)), choice('diagonal-shot', at(3, 3)), choice('melee', at(5, 4)),
    choice('push-back', at(5, 5), { isSuper: true }),
  ] }));
  assert.deepEqual(controls.attacks(), [
    { attackId: 'melee', isSuper: false },
    { attackId: 'diagonal-shot', isSuper: false },
    { attackId: 'push-back', isSuper: true },
  ]);
});

// ----- attacks with only one option (Push Back, Pull, Queen lines) ---------------------

const pull = choice('pull', at(5, 5), { isSuper: true, pushTo: at(4, 5) });
pull.pushes = [{ from: at(1, 5), to: at(4, 5) }];

test('an area attack fires when you click a piece it moves, not only your own square', () => {
  const controls = new TurnControls();
  controls.update(view({ attacks: [pull] }));
  controls.selectAttack('pull');
  assert.deepEqual(controls.click(at(1, 5)), {
    type: 'ATTACK', attackId: 'pull', aim: at(5, 5), landing: at(5, 5), pushTo: at(4, 5),
  });
});

test('an area attack fires when you click a square it damages', () => {
  const queen = choice('queen-lines', at(5, 5), { hits: [{ target: at(5, 9), halfHearts: 4 }] });
  const controls = new TurnControls();
  controls.update(view({ attacks: [queen] }));
  controls.selectAttack('queen-lines');
  assert.equal(controls.click(at(0, 0)), null, 'an unrelated square does nothing');
  assert.equal(controls.click(at(5, 9)).attackId, 'queen-lines');
});

test('picking an area attack again fires it; an aimed attack has nothing to confirm', () => {
  const controls = new TurnControls();
  controls.update(view({ attacks: [pull, choice('lane-shot', at(5, 9)), choice('lane-shot', at(1, 5))] }));
  controls.selectAttack('pull');
  assert.equal(controls.confirm().attackId, 'pull');
  controls.selectAttack('lane-shot');
  assert.equal(controls.confirm(), null);
});

test('the preview shows where pushed and pulled pieces end up', () => {
  const controls = new TurnControls();
  controls.update(view({ attacks: [pull] }));
  controls.selectAttack('pull');
  assert.deepEqual(controls.highlights(null).pushes, [{ from: at(1, 5), to: at(4, 5) }]);
});
