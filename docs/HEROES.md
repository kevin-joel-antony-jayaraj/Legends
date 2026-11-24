# Legends: Heroes

Status: draft. Numbers marked **TBD** are not decided yet. See [RULES.md](RULES.md) for the shared rules.

## Archer
- **Hearts:** 8
- **Move:** 1 square in any direction (like a king).
- **Normal attack, "Diagonal shot":** fires along a diagonal with unlimited range. Hits the first unit in its path. Blocked by forest and walls, not the river. Damage: 2 hearts.
- **Normal attack, "Melee":** 1 square, horizontal or vertical only. Damage: 2 hearts.
- **Super, "Push Back":** a shockwave. Every enemy within 2 squares (a solid 5×5 square around the Archer) takes 1 heart and is pushed straight away from the Archer until it's out of the square. A push stops early at a wall, a unit, or the river's edge.
  - **Charge:** full after the Archer deals damage in 5 hits.

## Knight (tank)
- **Hearts:** 12
- **Move:** chess knight L-jump (2 squares then 1 to the side, or 1 then 2). Jumps over pieces, the river, walls and bombs.
- **Normal attack, "Check":**
  - Can stop on any square along the L. Only the stop square is hit.
  - Damage by stop point: early on the L 1 heart, middle 2 hearts, last square 3 hearts.
  - Landing on the last square also hits every enemy within one square of it for 3 hearts.
  - The Knight takes the enemy's square and knocks the enemy back one square in the direction it's facing.
- **Super, "Bomb":**
  - Places a bomb on any empty square on the board.
  - Once placed, the bomb is hidden from everyone, including the Knight. Players must remember where it is.
  - Explodes when an enemy steps on it, for 3 hearts to that enemy.
  - Each blast covers the 3×3 square around its bomb: 3 hearts to every enemy in it.
  - Chain reaction: any other bomb inside that 3×3 square also explodes, with its own 3×3 blast. Bombs outside it don't.
  - Only one bomb per square. The Knight can't see its bombs either: it can pick a square that already has one, and gets the error "A bomb already exists on that square" (no action spent). If that leaks a bomb's position, that's the Knight player's problem.
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
  - A shot along any of the 8 lines, up to 4 squares. It hits the first enemy in line for 2 hearts. Blocked by forest and walls like an arrow, and can't be fired from inside a forest.
  - It then bounces to the nearest enemy within 2 squares of the last one hit, **doubling** the damage each time: 2, then 4, then 8 hearts.
  - Up to 3 hits per shot. Each enemy is hit at most once, and it never bounces back to the Pawn.
  - Bounces jump over walls and forest. If two enemies are equally near, it goes to the one higher up the board (then further left), so the preview always matches what happens.
  - Punishes players who bunch up: the third enemy in a chain takes 8 hearts.
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
1. **Knight's Check landing:** settled. The Knight takes the target's square and knocks it back (see the implementation notes).
2. **Teleport squares:** settled. The Pawn can teleport to any empty square, including next to enemies, except rivers and blocked tiles.

## Implementation notes
Choices made while coding v0.2 (`src/main/java/com/legends/hero/`) where this doc was not specific:
- **Knight movement:** a plain move is the full L-jump to its landing square. Stopping early along the L is part of the Check attack.
- **Knight's Check (decided in v0.4):** the Knight lands on the enemy's square and knocks the enemy back one square in the direction the Knight is facing (the direction of its last step into that square). Each end of the L can be reached two ways, so the player picks which way to knock. Damage by stop point is 1, 2 or 3 hearts, and hitting the end of the L splashes everyone next to the target for 3.
- **Blocked knockback:** if the square behind the enemy is a wall, river, board edge or another piece, the enemy stays put and the Knight lands on the square before it (or stays where it is, for an early stop).
- **Pawn evolving:** when its max hearts go up (8 to 10 to 12), the Pawn also gains the extra hearts. A Pawn on 5 of 8 hearts becomes 7 of 10.
- **Super meters:** a hit that deals no damage doesn't count toward the Archer's or Castle's hit count. The Knight's meter only counts health it actually lost.
- **Players:** the real game is 4 players, but setup also accepts 2 or 3, which is handy for testing.
- **Console letters:** A = Archer, K = Knight, C = Castle, W = Pawn (`P` is already the spawn-area tile).
- **Supers:** a Super costs one action, is only offered when charged, and empties the meter. Its own hits don't refill the meter.
- **Archer's Push Back:** an area effect, so it reaches pieces in forests and behind walls. "Straight away" means stepping in the direction from the Archer to the piece (diagonally if it isn't in line). The farthest pieces are pushed first, so they make room for the ones behind them.
- **Castle's Pull:** a wall between the Castle and a piece blocks the pull on that line. Forest doesn't. Rivers stop the pulled piece at the edge.
- **Bombs:** go off whenever an enemy arrives on the square, including by being pushed, knocked back or teleported.
- **Knife throw:** stays available until it's used, as an Assassin or an Engineer, because it's the only way to drop the Keystone. It's a ranged attack.
- **Poison:** ticks at the start of the victim's own turn, and can defeat them.
- **Keystone teleport:** lands on a random free square, and can land on a hidden bomb.
- **Multi-target attacks:** each piece hit counts as one hit for Super charging.
- **Ricochet range:** "within 2 squares" counts diagonals as 1 square, so it covers a 5×5 area around the target.
