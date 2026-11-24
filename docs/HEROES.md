# Legends: Heroes

Status: draft. Numbers marked **TBD** are not decided yet. See [RULES.md](RULES.md) for the shared rules.

## Archer
- **Hearts:** 8
- **Move:** 1 square in any direction (like a king).
- **Normal attack, "Diagonal shot":** fires along a diagonal, up to 4 squares. Hits the first unit in its path. Damage: 2 hearts.
- **Normal attack, "Melee":** 1 square, horizontal or vertical only. Damage: 2 hearts.
- **Super, "Push Back":** pushes a target up to 3 squares directly away from the Archer. Stops early at a wall, a unit, or the river's edge. Damage: 1 heart.
  - **Charge:** full after the Archer deals damage in 5 hits.

## Knight (tank)
- **Hearts:** 12
- **Move:** chess knight L-jump (2 squares then 1 to the side, or 1 then 2). Jumps over pieces, the river, walls and bombs.
- **Normal attack, "Check":**
  - Can stop on any square along the L. Only the stop square is hit.
  - Damage by stop point: early on the L 1 heart, middle 2 hearts, last square 3 hearts.
  - Landing on the last square also hits every enemy within one square of it for 3 hearts.
- **Super, "Bomb":**
  - Places a bomb on any empty square on the board.
  - Once placed, the bomb is hidden from everyone, including the Knight. Players must remember where it is.
  - Explodes when an enemy steps on it, for 3 hearts to that enemy.
  - Chain reaction: when a bomb explodes, every bomb within 2 squares of it also explodes, and each chained bomb deals 3 hearts to anyone in its blast.
  - The Knight takes no damage from its own bombs.
  - **Charge:** full after the Knight takes 6 hearts of damage. The meter charges from damage taken, but the bomb itself is still just a placement. Charging doesn't change what the bomb does.

## Castle (formerly Rook)
- **Hearts:** 10
- **Move:** slides in straight lines (up, down, left, right), up to 3 squares.
- **Normal, "Lane shot":**
  - Fires along a straight line with unlimited range. Hits the first unit in its path.
  - Blocked by forest and walls, not the river.
  - Within 2 squares: 2 hearts.
  - Beyond 2 squares: ½ heart.
  - Every hit charges the Super. Full charge after 4 hits.
- **Super, "Pull":**
  - Pulls the first enemy on each of the 4 straight lines toward the Castle.
  - Each pulled enemy stops on the square next to the Castle.
  - Stops at walls. Cannot pull enemies across the river.
  - No damage.

## Pawn
- **Move:** 1 square in any direction, like a chess king. The Pawn has no forward direction.
- **Evolution (stages):** the Pawn evolves as the game goes on. Each stage raises its max hearts and changes its attack. Stages last for the rest of the game.

### Stage 1: Assassin (8 hearts)
- **Normal, "Knife":**
  - Melee reach 2 in any direction, in a straight line. Adjacent: 2 hearts. Two squares away: 1 heart.
  - **Throw (one per game):** the Knife can be thrown in any of the 8 directions, with unlimited range, hitting the first unit in its path for 1 heart.
  - **Poison:** the target is poisoned for 3 rounds, taking 1 heart at the start of each round. That's 3 hearts total. Poison does not stack.
  - Throwing the Knife leaves it on the board as a **Keystone**, on the target's square.
  - Any hit evolves the Pawn to Stage 2 (see below).

### Stage 2: Engineer (10 hearts, after the Pawn's first hit)
- **Trigger:** the Pawn's first hit of any kind.
- **Normal, "Ricochet chain":**
  - Pick one adjacent enemy in any direction and hit it for 3 hearts.
  - The hit then chains to every other enemy within 2 squares of the target, for 1.5 hearts each.
  - The chain can hit several players at once, which punishes players who team up against the Pawn.
- The Keystone stays on the board.

### Stage 3: Queen (12 hearts, after the Pawn touches the Keystone)
- **Trigger:** the Pawn walks onto the Keystone's square.
- **On evolving:** the Pawn teleports to a random empty square on the board, except rivers and other blocked tiles. This keeps it from being cornered right after it evolves.
- **Normal, "Queen lines":**
  - Attacks in all 8 directions. The first enemy on each line takes 2 hearts.
  - Range capped at 4 squares.
- **Keystone:** disappears when the Pawn evolves to Queen.

### The Keystone
- **Any other player** who walks onto the Keystone is fully healed. There's no limit on how many times a player can use it.
- **No cooldown.** The Keystone can be used again on the next turn, so a random placement can be a great or poor spot.
- The Keystone **stays on the board** if the Pawn dies, and it can't be attacked.
- Only the Pawn walking onto it evolves the Pawn to Queen, and then the Keystone disappears.
- Players race to the Keystone and defend it, and the Pawn has to fight its way into it.

## Open questions
1. **Teleport squares:** settled. The Pawn can teleport to any empty square, including next to enemies, except rivers and blocked tiles.

## Implementation notes
Choices made while coding v0.2 (`src/main/java/com/legends/hero/`) where this doc was not specific:
- **Knight movement:** a plain move is the full L-jump to its landing square. Stopping early along the L belongs to the "Check" attack, which comes with attacks in v0.3.
- **Pawn evolving:** when its max hearts go up (8 to 10 to 12), the Pawn also gains the extra hearts. A Pawn on 5 of 8 hearts becomes 7 of 10.
- **Super meters:** a hit that deals no damage doesn't count toward the Archer's or Castle's hit count. The Knight's meter only counts health it actually lost.
- **Players:** the real game is 4 players, but setup also accepts 2 or 3, which is handy for testing.
- **Console letters:** A = Archer, K = Knight, C = Castle, W = Pawn (`P` is already the spawn-area tile).
