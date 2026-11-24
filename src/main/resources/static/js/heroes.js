// Display names and colours. Only presentation lives here: the rules are all on the server.

/** One colour per seat, in seat order (seat 0 is the first player who joined, not who moves first). */
export const SEAT_COLORS = ['#e0574c', '#4a8be4', '#e5b53a', '#55b467'];

const HERO_NAMES = { ARCHER: 'Archer', KNIGHT: 'Knight', CASTLE: 'Castle', PAWN: 'Pawn' };
const PAWN_STAGES = { ASSASSIN: 'Assassin', ENGINEER: 'Engineer', QUEEN: 'Queen' };
const ATTACK_NAMES = {
  'diagonal-shot': 'Diagonal shot',
  melee: 'Melee',
  'push-back': 'Push Back',
  check: 'Check',
  bomb: 'Bomb',
  'lane-shot': 'Lane shot',
  pull: 'Pull',
  knife: 'Knife',
  'knife-throw': 'Knife throw',
  ricochet: 'Ricochet',
  'queen-lines': 'Queen lines',
};

export const HERO_TYPES = Object.keys(HERO_NAMES);

export function heroTypeName(type) {
  return HERO_NAMES[type] ?? type;
}

export function pawnStageName(stage) {
  return PAWN_STAGES[stage] ?? stage;
}

export function attackName(attackId) {
  return ATTACK_NAMES[attackId] ?? attackId;
}

/** "Ana (Archer)" */
export function heroLabel(hero) {
  return `${hero.name} (${heroTypeName(hero.type)})`;
}

/** The letter drawn on the board. The Pawn's letter shows its stage. */
export function heroLetter(hero) {
  switch (hero.type) {
    case 'ARCHER': return 'A';
    case 'KNIGHT': return 'K';
    case 'CASTLE': return 'C';
    case 'PAWN': return hero.pawnStage === 'QUEEN' ? 'Q' : hero.pawnStage === 'ENGINEER' ? 'E' : 'P';
    default: return '?';
  }
}

/** Half-hearts as hearts: 7 -> "3.5", 8 -> "4". */
export function formatHearts(halfHearts) {
  return halfHearts % 2 === 0 ? String(halfHearts / 2) : (halfHearts / 2).toFixed(1);
}
