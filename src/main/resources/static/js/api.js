// Talks to the server's REST API (docs/API.md). Every request either returns the parsed
// JSON or throws an ApiError with the server's message, like "It's not your turn".

const TOKEN_HEADER = 'X-Player-Token';

export class ApiError extends Error {
  /** @param status the HTTP status, or 0 if the server couldn't be reached */
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

async function call(path, { method = 'GET', token = null, body } = {}) {
  const headers = {};
  if (token) headers[TOKEN_HEADER] = token;
  if (body !== undefined) headers['Content-Type'] = 'application/json';

  let response;
  try {
    response = await fetch(path, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
  } catch {
    throw new ApiError(0, "Can't reach the server. Is it still running?");
  }

  const text = await response.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    // Not JSON, for example a proxy's error page; fall through to the status check.
  }
  if (!response.ok) {
    throw new ApiError(response.status, data?.error ?? `The server answered ${response.status}`);
  }
  return data;
}

/** Starts a game. Returns {gameId, seed, seats: [{seat, name, hero, token}]}. */
export function createGame(request) {
  return call('/api/games', { method: 'POST', body: request });
}

/** Returns {lanAddresses, port}: how other computers can reach this server. */
export function serverInfo() {
  return call('/api/server');
}

/** One player's connection to one game. */
export class GameClient {
  #gameId;
  #token;

  constructor(gameId, token) {
    this.#gameId = gameId;
    this.#token = token;
  }

  get gameId() {
    return this.#gameId;
  }

  /** The game as this player may see it. */
  view() {
    return call(`/api/games/${encodeURIComponent(this.#gameId)}`, { token: this.#token });
  }

  /** Sends an action like {type: "MOVE", to: {row, col}}. Returns {events, view}. */
  act(action) {
    return call(`/api/games/${encodeURIComponent(this.#gameId)}/actions`,
      { method: 'POST', token: this.#token, body: action });
  }
}
