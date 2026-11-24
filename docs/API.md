# Legends: Server API

The server wraps the game engine for the network. All rules run on the server; a client only shows the game and sends requests.

Start it with `mvn spring-boot:run` (or `java -jar target/legends-*.jar` after `mvn package`). It listens on http://localhost:8080, which also serves the browser client: the lobby at `/` and the game at `/play.html?game={gameId}&token={token}`.

## Who is asking: player tokens
Creating a game returns a secret **token** for each seat. Send it with every request in the `X-Player-Token` header. Whoever has a seat's token can play that seat, so give each player only their own.

## Create a game
`POST /api/games`

```json
{"seed": 42, "players": [
  {"name": "Ana",  "hero": "ARCHER"},
  {"name": "Ben",  "hero": "KNIGHT"},
  {"name": "Cleo", "hero": "CASTLE"},
  {"name": "Dev",  "hero": "PAWN"}]}
```
- 2 to 4 players, in clockwise seat order. Heroes: `ARCHER`, `KNIGHT`, `CASTLE`, `PAWN`.
- `seed` is optional. Leave it out for a random board; random seeds are below 1,000,000,000 so they're easy to share and JavaScript holds them exactly.

Response `201`:
```json
{"gameId": "95f2c09c-...", "seed": 42, "seats": [
  {"seat": 0, "name": "Ana", "hero": "ARCHER", "token": "..."}, ...]}
```

## See the game
`GET /api/games/{gameId}` with `X-Player-Token`

Returns what that player is allowed to see:
- `board`: 12 rows of tile types (`GRASS`, `SPAWN`, `RIVER`, `BRIDGE`, `FOREST`, `WALL`)
- `heroes`: every hero's seat, name, type, hearts (in half-hearts), position, Super progress, poison, and Pawn stage
- `keystone`: where the Keystone is, or `null`
- `currentSeat`, `actionsLeft`, `over`, `winnerSeat`
- `yourMoves`, `yourAttacks`: filled in only when it's your turn

Hidden bombs never appear in a view.

## Act
`POST /api/games/{gameId}/actions` with `X-Player-Token`

```json
{"type": "MOVE", "to": {"row": 2, "col": 2}}
{"type": "ATTACK", "attackId": "diagonal-shot", "aim": {"row": 3, "col": 3}}
{"type": "ATTACK", "attackId": "check", "aim": {"row": 3, "col": 6}, "landing": {"row": 3, "col": 6}, "pushTo": {"row": 3, "col": 7}}
{"type": "END_TURN"}
```
For an attack, copy `attackId`, `aim`, `landing` and `pushTo` from one of the `yourAttacks` entries.

Response `200`: the `events` that happened, in order, and your updated `view`.
```json
{"events": [{"type": "HeroMoved", "data": {"seat": 1, "from": {"row": 1, "col": 10}, "to": {"row": 0, "col": 8}}}],
 "view": {...}}
```

## Live updates (WebSocket)
Connect with STOMP to `ws://localhost:8080/ws` and subscribe to `/topic/games/{gameId}`. After every action, the server publishes that action's events (the same list as above) to everyone subscribed.

## Errors
Every error has a body like `{"error": "It's not your turn"}`.

| Status | Meaning |
|---|---|
| 400 | The request is malformed or missing a field |
| 403 | The player token is missing or not part of this game |
| 404 | No game with that id |
| 409 | The request is fine, but the rules don't allow it right now |

## Limits for now
- Games live in memory and are lost when the server restarts.
- The game creator receives every seat's token and hands them out. A proper lobby (join a game, pick a seat) comes later.

## Server info
`GET /api/server` (no token needed)

```json
{"lanAddresses": ["192.168.1.20"], "port": 8080}
```
This computer's addresses on the local network, and the port the request came in on. The lobby uses it to build join links that work from other computers, since `localhost` only works on the server's own machine.
