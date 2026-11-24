import assert from 'node:assert/strict';
import { test } from 'node:test';
import { describeEvent } from '../../main/resources/static/js/event-text.js';

const heroes = [{ seat: 0, name: 'Ana', type: 'ARCHER' }, { seat: 1, name: 'Ben', type: 'KNIGHT' }];

test('damage reads as a sentence with hearts', () => {
  const event = { type: 'HeroDamaged',
    data: { targetSeat: 1, attackerSeat: 0, attackId: 'diagonal-shot', halfHearts: 4, halfHeartsLeft: 19 } };
  assert.equal(describeEvent(event, heroes), "Ben (Knight) takes 2 ♥ from Ana (Archer)'s Diagonal shot (9.5 left)");
});

test("a placed bomb's event says nothing about where", () => {
  assert.equal(describeEvent({ type: 'BombPlaced', data: { seat: 1 } }, heroes), 'Ben (Knight) hides a bomb somewhere…');
});

test('a game with no survivors says so', () => {
  assert.equal(describeEvent({ type: 'GameOver', data: { winnerSeat: -1 } }, heroes), 'Game over: nobody survived.');
});
