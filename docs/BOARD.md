# Legends: Board

Status: draft. The board is fixed for version 1 but **randomly generated** at the start of each game. Terrain events (for example, a flooding river) come later.

See [RULES.md](RULES.md) for the shared rules and [HEROES.md](HEROES.md) for the heroes.

## Overview
- 12×12 grid, rows 0–11 (top to bottom), columns 0–11 (left to right).
- Four players, each starting in one corner.
- The four corners are **not** symmetric. Each game randomly picks what each corner is.
- The centre is kept open. Walls and forest are placed randomly around it.

## Tile types

| Symbol | Tile | Walkable | Blocks arrows | Notes |
|---|---|---|---|---|
| `.` | Grass | Yes | No | Normal ground |
| `P` | Spawn area | Yes | No | The irregular patch of ground inside a river corner |
| `S` | Start square | Yes | No | Where a hero starts. One square in from the corner. |
| `~` | River | No | No | Arrows fly over. Castle pull can't cross it. |
| `=` | Bridge | Yes | No | The only walkable crossing over the river |
| `F` | Forest | Yes | Partly | Cover. Ranged shots can't go into it, so a unit inside can only be hit by melee, the Knight, bombs or area attacks. A unit inside **can** shoot out, but not into the next forest square. Melee reach (the Knife) passes through it. |
| `#` | Wall | No | Yes | Permanent for now. Also blocks melee reach. |

## Corner types
Each game, the four corners are assigned randomly as:
- **2 river corners:** an irregular spawn area with a river moat around its inner sides and one bridge.
  - **River shape:** random and winding, not a straight line.
  - **River width:** 1 or 2 squares, chosen randomly per river, so some moats are thick and some thin.
  - **Bridge:** placed on the moat where the river crosses the spawn area's edge, facing the middle.
- **1 forest corner:** an irregular patch of forest.
- **1 open corner:** plain grass.

Shapes are **irregular blobs**, not perfect squares. The hero starts one square in from its corner, diagonally, so it's never on the board's edge.

## Centre generation
- **Open core:** the central 4×4 area (rows 4–7, columns 4–7) stays grass. At most one wall can touch it.
- **Walls:** **3 to 5** straight segments of 1–4 tiles, placed randomly around the core. Segments can be horizontal or vertical. Walls may touch a corner's blob.
- **Forest:** randomly shaped patches of 2–4 tiles in the middle ring.
- **Bridges:** each river corner gets one bridge on one of its two moat sides, chosen so it **faces the middle of the board**.

## Validity checks
After generating a board, check these before the game starts. If any fail, generate again.
1. Every hero can reach every other hero's start square by walking or using bridges. Walls and rivers can't seal a corner.
2. Each hero has at least one walkable square next to its start square, and a Knight starting there would have at least one square to jump to.
3. No two heroes start on the same square.

## Sample board
Illustrative only. Real boards will have irregular corner shapes and randomised walls and forest. Top-left and bottom-left are river corners, top-right is forest, bottom-right is open.

```
     0 1 2 3 4 5 6 7 8 9 10 11
 0   P P P ~ . . . . . F F  F
 1   P S P ~ . . . . . F S  F
 2   P P P ~ . . . . . F F  F
 3   ~ = ~ ~ . . . . . . .  .
 4   . . . . . F F . . . .  .
 5   . . . . . . . . # . .  .
 6   . . # . . . . . . F .  .
 7   . . . . . . . F F . .  .
 8   ~ = ~ ~ . . . . . . .  .
 9   P P P ~ . . . . . . .  .
10   P S P ~ . . . . . . S  .
11   P P P ~ . . . . . . .  .
```

## Reproducibility
- Each board is generated from a **random seed**. Store the seed with each game, so you can regenerate the same board for a bug report or a replay.
- A good first test: the same seed always produces the same board.

## Size rules
- **Corner blobs:** each stretches **3 to 5 squares** in from its corner edge. The same size range applies to all corners.
- **Forest corner:** the same size as a river corner's spawn area, so no corner type is bigger.
- **Walls:** may touch a corner blob. This is intentional, since it creates awkward spots to fight over.

## Open questions
None right now. Next up: the spawn-area shape for the river corners, if you want to pick one.

## Implementation notes
Choices made while coding the generator (`src/main/java/com/legends/board/`) where this doc was not specific:
- **Corner blob shape:** a "staircase". Each row is no wider than the row above it, and rows 0–2 are at least 3 wide so the start square and its neighbours are always inside. The inner corner is trimmed so the moat never reaches the open core.
- **Moat:** the first ring of water covers all 8 neighbours of the spawn area, so not even a diagonal step can get past it. A 2-wide moat's second ring stays out of the core.
- **Bridge:** a straight line out of the spawn area, toward the middle. The generator prefers bridges that don't run along the board's outer edge, and among those picks the shortest crossing.
- **Walls in the core:** at most one wall segment may enter the core.
- **Forest patches:** 2–4 patches in the middle ring, never in the core.
- **Fairness check:** reachability is tested with orthogonal steps only (how the Castle moves). If the Castle can get somewhere, every hero can.
