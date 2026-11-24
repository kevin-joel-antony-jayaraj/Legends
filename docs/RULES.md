# Legends: Core Rules

Status: draft. Items marked **TBD** are not decided yet. Hero stats are in [HEROES.md](HEROES.md), and the board is described in [BOARD.md](BOARD.md).

## Overview
- Turn-based tactics on a 12×12 grid, medieval theme.
- 4 players, free-for-all, one hero each. Last hero standing wins.
- Chess-like movement, but attacks deal damage from the attacker's square.

## Board
- 12×12 grid, randomly generated at the start of each game. See [BOARD.md](BOARD.md).
- Each player starts one square in from a corner.
- Tile types: grass, spawn area, river, bridge, forest, wall.
- The board is fixed during a game for version 1. Terrain events come later.

## Pieces and squares
- Only one piece per square.
- Attacking never moves the attacker.
- Hearts are counted in half-hearts (1 heart = 2 half-hearts). Starting hearts per hero are in [HEROES.md](HEROES.md).
- A piece with 0 hearts is removed from the board.

## Turns
- Turn order is **fixed**: a random player starts each game, then play goes clockwise.
- If a hero has no legal action on its turn, the turn passes.
- Using a Super costs one action, the same as a Normal attack or a move.
- Poison damage is applied at the start of the poisoned hero's own turn: 1 heart each time, for 3 rounds (3 hits total).
- Each hero gets a **fixed number of actions per turn**. An action is one move or one attack (or one Super).
- The action count is hardcoded per hero, based on its power. Heroes with strong or long moves get fewer actions, and slower heroes get more.
- Draft action counts (**TBD**, to balance in play):
  - Archer: 3
  - Knight: 2 (each L-jump is one action, so 12 is far too many)
  - Castle: 3
  - Pawn: 4 (it moves 1 square at a time, so it needs more actions)
- **No turn limit.** The game ends only when one hero is left standing.

## Supers
- Each hero has a Normal attack and a Super.
- Each hero's Super meter charges differently, so it fits the hero's playstyle. Charge rules are in [HEROES.md](HEROES.md):
  - **Archer:** full after dealing damage in 5 hits.
  - **Knight:** full after taking 6 hearts of damage.
  - **Castle:** full after 4 hits.
- The Pawn has no Super. Its Surge stages replace it.

## Eliminated players
- A player whose hero reaches 0 hearts is **out of the game** and stays out. They don't take turns or act, and they can't affect the board.
- The Keystone stays on the board when the Pawn dies, so a dead Pawn's Keystone is still a race for the rest of the players.

## Deferred (not in version 1)
- Gamemaster mode (queen-like boss and swarm).
- Mario Party-style arena fights.
- Cleric hero (dropped for the free-for-all version).
- Dice or variable actions.
- Armor.
- Terrain events (for example, a flooding river).
- 3D board and multiplayer over the network.
