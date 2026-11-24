// A small STOMP client over a plain WebSocket, written by hand so the game needs no
// downloads and works on a LAN without internet.
//
// STOMP is a text protocol. A frame looks like:
//
//   COMMAND
//   header:value
//
//   body^@        (^@ is the NUL character, which ends every frame)
//
// We only need four commands: CONNECT (we send), CONNECTED (server replies),
// SUBSCRIBE (we send) and MESSAGE (server pushes game events).

const NUL = '\u0000';

/** Builds one frame as text. */
export function encodeFrame(command, headers = {}, body = '') {
  const headerLines = Object.entries(headers).map(([name, value]) => `${name}:${value}`);
  return [command, ...headerLines, '', body].join('\n') + NUL;
}

/**
 * Splits WebSocket data into frames: [{command, headers, body}]. A message can hold
 * several frames, and bare newlines between frames are heart-beats, which are skipped.
 */
export function parseFrames(data) {
  return data.split(NUL)
    .map((chunk) => chunk.replace(/^[\r\n]+/, ''))
    .filter((chunk) => chunk.length > 0)
    .map(parseFrame);
}

function parseFrame(text) {
  const normalized = text.replace(/\r\n/g, '\n');
  const split = normalized.indexOf('\n\n');
  const head = split === -1 ? normalized : normalized.slice(0, split);
  const body = split === -1 ? '' : normalized.slice(split + 2);
  const [command, ...headerLines] = head.split('\n');
  const headers = {};
  for (const line of headerLines) {
    const colon = line.indexOf(':');
    const name = line.slice(0, colon);
    // STOMP 1.2: if a header repeats, the first one counts.
    if (colon > 0 && !(name in headers)) headers[name] = line.slice(colon + 1);
  }
  return { command, headers, body };
}

/**
 * Connects to a STOMP endpoint and keeps trying to reconnect if the connection drops.
 * Subscriptions are remembered and renewed after every reconnect.
 */
export class StompClient {
  #url;
  #onStatus;
  #socket = null;
  #subscriptions = new Map();
  #nextId = 0;
  #connected = false;
  #closed = false;
  #retryMs = 500;

  /**
   * @param url      like "ws://192.168.1.20:8080/ws"
   * @param onStatus called with "connecting", "live" or "offline"
   */
  constructor(url, { onStatus = () => {} } = {}) {
    this.#url = url;
    this.#onStatus = onStatus;
  }

  connect() {
    this.#closed = false;
    this.#onStatus('connecting');
    const socket = new WebSocket(this.#url, ['v12.stomp']);
    this.#socket = socket;

    socket.onopen = () => {
      socket.send(encodeFrame('CONNECT', {
        'accept-version': '1.2',
        host: new URL(this.#url).hostname,
        'heart-beat': '0,0',
      }));
    };
    socket.onmessage = (message) => {
      for (const frame of parseFrames(String(message.data))) this.#handle(frame);
    };
    socket.onclose = () => {
      if (this.#socket !== socket) return; // an old socket closing after a reconnect
      this.#connected = false;
      if (this.#closed) return;
      this.#onStatus('offline');
      setTimeout(() => this.connect(), this.#retryMs);
      this.#retryMs = Math.min(this.#retryMs * 2, 8000);
    };
  }

  /** Calls {@code onMessage} with the parsed JSON body of every message sent to the destination. */
  subscribe(destination, onMessage) {
    const id = `sub-${this.#nextId++}`;
    this.#subscriptions.set(id, { destination, onMessage });
    if (this.#connected) this.#sendSubscribe(id, destination);
  }

  close() {
    this.#closed = true;
    this.#socket?.close();
  }

  #handle(frame) {
    switch (frame.command) {
      case 'CONNECTED':
        this.#connected = true;
        this.#retryMs = 500;
        for (const [id, { destination }] of this.#subscriptions) this.#sendSubscribe(id, destination);
        this.#onStatus('live');
        break;
      case 'MESSAGE': {
        const subscription = this.#subscriptions.get(frame.headers.subscription);
        if (subscription) subscription.onMessage(JSON.parse(frame.body));
        break;
      }
      case 'ERROR':
        console.error('STOMP error:', frame.headers.message, frame.body);
        break;
      default:
        break;
    }
  }

  #sendSubscribe(id, destination) {
    this.#socket.send(encodeFrame('SUBSCRIBE', { id, destination, ack: 'auto' }));
  }
}
