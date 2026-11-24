import assert from 'node:assert/strict';
import { test } from 'node:test';
import { encodeFrame, parseFrames } from '../../main/resources/static/js/stomp.js';

test('a frame survives being encoded and parsed', () => {
  const text = encodeFrame('SEND', { destination: '/topic/x' }, '{"a":1}');
  assert.deepEqual(parseFrames(text), [{ command: 'SEND', headers: { destination: '/topic/x' }, body: '{"a":1}' }]);
});

test('heart-beats and several frames in one message are handled', () => {
  const data = '\n' + encodeFrame('CONNECTED', { version: '1.2' }) + '\n\n'
    + encodeFrame('MESSAGE', { subscription: 'sub-0', 'content-type': 'application/json' }, '[]');
  const frames = parseFrames(data);
  assert.deepEqual(frames.map((f) => f.command), ['CONNECTED', 'MESSAGE']);
  assert.equal(frames[1].headers.subscription, 'sub-0');
  assert.equal(frames[1].body, '[]');
});

test('header values may contain colons, and the first repeated header wins', () => {
  const [frame] = parseFrames('MESSAGE\nmessage-id:a:b\nx:1\nx:2\n\n\u0000');
  assert.equal(frame.headers['message-id'], 'a:b');
  assert.equal(frame.headers.x, '1');
});
