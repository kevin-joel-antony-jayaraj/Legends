# Legends
A 2-D game I'm building during my Java lectures : )

## Playing it
Needs Java 21+ and Maven (`brew install maven`).

1. Start the server: `mvn spring-boot:run`
2. Open http://localhost:8080, pick 2 to 4 players and heroes, and click **Create game**.
3. Send each player their own link. Everyone on the same Wi-Fi can play from their own computer; the server prints its network address when it starts (like `http://192.168.1.20:8080`). To play alone or hot-seat, open every link in its own tab.

If macOS asks whether Java may accept incoming connections, allow it, or other computers can't connect.

## Developing
- Java tests: `mvn test`
- Browser client tests: `node --test 'src/test/js/*.test.js'` (needs Node 22+)
- Server API: [docs/API.md](docs/API.md)
- Terminal version: run `com.legends.Main` (optionally pass a seed number to replay a board)

### How the code is split
| Part | Where | Job |
|---|---|---|
| Game rules | `com.legends.board`, `hero`, `game`, `engine` | Plain Java, no Spring. The only place rules live. |
| Server | `com.legends.server` | REST + WebSocket around the engine. Decides what each player may see. |
| Browser client | `src/main/resources/static` | Draws the game and sends clicks. No rules: it only picks from the moves and attacks the server lists. |
| Terminal client | `com.legends.cli` | The same engine, played in a terminal. |

Inside the browser client (`static/js`):
- `api.js`: REST calls. `stomp.js`: live updates over the WebSocket (a tiny hand-written STOMP client, so it works offline on a LAN).
- `turn-controls.js`: what a click means. Testable without a browser.
- `board-renderer.js`: draws the board on a canvas. Pixel art later only changes this file.
- `panels.js`, `event-text.js`: side panels and the event log.
- `play.js`, `lobby.js`: wire each page together.

Game design: [docs/RULES.md](docs/RULES.md), [docs/HEROES.md](docs/HEROES.md), [docs/BOARD.md](docs/BOARD.md).
