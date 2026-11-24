// Draws the board on a <canvas>. It only paints what it's given (a view from the server
// plus highlights from TurnControls), so swapping in pixel-art sprites later means
// changing this file and nothing else.

import { SEAT_COLORS, heroLetter } from './heroes.js';
import { same } from './squares.js';

const TILE = 52;
const MARGIN = 22; // room for row and column numbers

const TILE_COLORS = {
  GRASS: ['#5d8c44', '#58863f'], // two shades, alternating like a chessboard
  SPAWN: ['#7aa65a', '#73a053'],
  BRIDGE: ['#9b6b3d', '#9b6b3d'],
  RIVER: ['#3a6fb0', '#3a6fb0'],
  FOREST: ['#2e5a30', '#2b562d'],
  WALL: ['#6f6b66', '#6f6b66'],
};

export class BoardRenderer {
  #canvas;
  #ctx;
  #rows = 0;
  #cols = 0;

  constructor(canvas) {
    this.#canvas = canvas;
    this.#ctx = canvas.getContext('2d');
  }

  /** The square under a mouse position, or null if it's off the board. */
  squareAt(clientX, clientY) {
    const rect = this.#canvas.getBoundingClientRect();
    const scale = this.#size().width / rect.width;
    const col = Math.floor(((clientX - rect.left) * scale - MARGIN) / TILE);
    const row = Math.floor(((clientY - rect.top) * scale - MARGIN) / TILE);
    return row >= 0 && row < this.#rows && col >= 0 && col < this.#cols ? { row, col } : null;
  }

  /**
   * @param view       the game view from the server
   * @param highlights from TurnControls.highlights()
   */
  draw(view, highlights) {
    this.#rows = view.board.length;
    this.#cols = view.board[0].length;
    this.#resize();
    const ctx = this.#ctx;
    const { width, height } = this.#size();
    ctx.fillStyle = '#1b1d22';
    ctx.fillRect(0, 0, width, height);

    this.#drawLabels();
    view.board.forEach((row, r) => row.forEach((tile, c) => this.#drawTile(tile, r, c)));
    const living = view.heroes.filter((h) => h.alive && h.position);
    const keystoneCovered = view.keystone && living.some((h) => same(h.position, view.keystone));
    if (view.keystone && !keystoneCovered) this.#drawKeystone(view.keystone);

    highlights.moves.forEach((square) => this.#dot(square, 'rgba(255,255,255,0.75)'));
    highlights.aims.forEach((square) => this.#outline(square, 'rgba(255,170,60,0.9)', 2));
    highlights.picks.forEach((square) => this.#outline(square, '#8fe0ff', 3));
    if (highlights.selected) this.#outline(highlights.selected, '#ffffff', 3);

    const current = view.over ? null : view.currentSeat;
    living.forEach((hero) => this.#drawHero(hero, hero.seat === current));
    if (keystoneCovered) this.#drawKeystone(view.keystone, true); // small, in the corner, so it stays visible

    if (highlights.landing) this.#ghost(highlights.landing, SEAT_COLORS[view.yourSeat]);
    highlights.hits.forEach((hit) => this.#hit(hit));
  }

  // ----- layout -------------------------------------------------------------------

  #size() {
    return { width: MARGIN + this.#cols * TILE, height: MARGIN + this.#rows * TILE };
  }

  /** Matches the canvas's pixel size to the screen so it stays sharp on high-DPI displays. */
  #resize() {
    const { width, height } = this.#size();
    const ratio = window.devicePixelRatio || 1;
    if (this.#canvas.width !== width * ratio || this.#canvas.height !== height * ratio) {
      this.#canvas.width = width * ratio;
      this.#canvas.height = height * ratio;
      this.#canvas.style.width = `${width}px`;
      this.#canvas.style.aspectRatio = `${width} / ${height}`;
    }
    this.#ctx.setTransform(ratio, 0, 0, ratio, 0, 0);
  }

  /** Top-left corner of a square, in canvas units. */
  #corner(square) {
    return { x: MARGIN + square.col * TILE, y: MARGIN + square.row * TILE };
  }

  #center(square) {
    const { x, y } = this.#corner(square);
    return { x: x + TILE / 2, y: y + TILE / 2 };
  }

  // ----- tiles --------------------------------------------------------------------

  #drawLabels() {
    const ctx = this.#ctx;
    ctx.fillStyle = '#8a8f99';
    ctx.font = '11px ui-monospace, Menlo, monospace';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    for (let c = 0; c < this.#cols; c++) ctx.fillText(String(c), MARGIN + c * TILE + TILE / 2, MARGIN / 2);
    for (let r = 0; r < this.#rows; r++) ctx.fillText(String(r), MARGIN / 2, MARGIN + r * TILE + TILE / 2);
  }

  #drawTile(tile, row, col) {
    const ctx = this.#ctx;
    const { x, y } = this.#corner({ row, col });
    const shades = TILE_COLORS[tile] ?? ['#ff00ff', '#ff00ff'];
    ctx.fillStyle = shades[(row + col) % 2];
    ctx.fillRect(x, y, TILE, TILE);

    switch (tile) {
      case 'RIVER': this.#waves(x, y); break;
      case 'BRIDGE': this.#planks(x, y); break;
      case 'FOREST': this.#trees(x, y); break;
      case 'WALL': this.#bricks(x, y); break;
      default: break;
    }
  }

  #waves(x, y) {
    const ctx = this.#ctx;
    ctx.strokeStyle = 'rgba(255,255,255,0.22)';
    ctx.lineWidth = 2;
    for (const dy of [16, 34]) {
      ctx.beginPath();
      ctx.moveTo(x + 8, y + dy);
      ctx.quadraticCurveTo(x + 17, y + dy - 5, x + 26, y + dy);
      ctx.quadraticCurveTo(x + 35, y + dy + 5, x + 44, y + dy);
      ctx.stroke();
    }
  }

  #planks(x, y) {
    const ctx = this.#ctx;
    ctx.strokeStyle = 'rgba(0,0,0,0.3)';
    ctx.lineWidth = 1;
    for (let i = 1; i < 4; i++) {
      ctx.beginPath();
      ctx.moveTo(x, y + (TILE / 4) * i);
      ctx.lineTo(x + TILE, y + (TILE / 4) * i);
      ctx.stroke();
    }
  }

  #trees(x, y) {
    const ctx = this.#ctx;
    ctx.fillStyle = '#1f4421';
    for (const [dx, dy] of [[15, 20], [36, 17], [26, 38]]) {
      ctx.beginPath();
      ctx.moveTo(x + dx, y + dy - 10);
      ctx.lineTo(x + dx + 8, y + dy + 6);
      ctx.lineTo(x + dx - 8, y + dy + 6);
      ctx.closePath();
      ctx.fill();
    }
  }

  #bricks(x, y) {
    const ctx = this.#ctx;
    ctx.strokeStyle = 'rgba(0,0,0,0.35)';
    ctx.lineWidth = 1;
    const course = TILE / 4;
    for (let i = 0; i < 4; i++) {
      const top = y + i * course;
      ctx.strokeRect(x + 0.5, top + 0.5, TILE - 1, course);
      const offset = i % 2 === 0 ? TILE / 2 : TILE / 4;
      for (let bx = x + offset; bx < x + TILE; bx += TILE / 2) {
        ctx.beginPath();
        ctx.moveTo(bx, top);
        ctx.lineTo(bx, top + course);
        ctx.stroke();
      }
    }
  }

  /** The Keystone: a gold diamond, full size on an empty square, or small in the corner under a hero. */
  #drawKeystone(square, small = false) {
    const ctx = this.#ctx;
    const { x: left, y: top } = this.#corner(square);
    const x = small ? left + 10 : left + TILE / 2;
    const y = small ? top + 11 : top + TILE / 2;
    const size = small ? 0.65 : 1;
    ctx.fillStyle = '#ffd54a';
    ctx.strokeStyle = '#7a5a00';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(x, y - 14 * size);
    ctx.lineTo(x + 11 * size, y);
    ctx.lineTo(x, y + 14 * size);
    ctx.lineTo(x - 11 * size, y);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();
  }

  // ----- heroes -------------------------------------------------------------------

  #drawHero(hero, isCurrent) {
    const ctx = this.#ctx;
    const { x, y } = this.#center(hero.position);
    const radius = TILE * 0.34;

    if (isCurrent) {
      ctx.strokeStyle = '#ffffff';
      ctx.lineWidth = 3;
      ctx.beginPath();
      ctx.arc(x, y - 3, radius + 5, 0, Math.PI * 2);
      ctx.stroke();
    }
    if (hero.hasSuper && hero.superProgress >= 1) {
      ctx.strokeStyle = '#ffd54a';
      ctx.lineWidth = 2;
      ctx.setLineDash([4, 3]);
      ctx.beginPath();
      ctx.arc(x, y - 3, radius + 2, 0, Math.PI * 2);
      ctx.stroke();
      ctx.setLineDash([]);
    }

    ctx.fillStyle = SEAT_COLORS[hero.seat];
    ctx.strokeStyle = 'rgba(0,0,0,0.6)';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.arc(x, y - 3, radius, 0, Math.PI * 2);
    ctx.fill();
    ctx.stroke();

    ctx.fillStyle = '#ffffff';
    ctx.font = 'bold 18px system-ui, sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(heroLetter(hero), x, y - 2);

    // Health bar along the bottom of the square.
    const barWidth = TILE - 14;
    const left = x - barWidth / 2;
    const top = y + TILE / 2 - 8;
    ctx.fillStyle = 'rgba(0,0,0,0.55)';
    ctx.fillRect(left, top, barWidth, 5);
    ctx.fillStyle = healthColor(hero.halfHearts / hero.maxHalfHearts);
    ctx.fillRect(left, top, barWidth * (hero.halfHearts / hero.maxHalfHearts), 5);

    if (hero.poisonTurnsLeft > 0) {
      ctx.fillStyle = '#7dff5a';
      ctx.strokeStyle = '#173d0c';
      ctx.lineWidth = 1.5;
      ctx.beginPath();
      ctx.arc(x + radius, y - radius - 2, 5, 0, Math.PI * 2);
      ctx.fill();
      ctx.stroke();
    }
  }

  // ----- highlights ---------------------------------------------------------------

  #dot(square, color) {
    const ctx = this.#ctx;
    const { x, y } = this.#center(square);
    ctx.fillStyle = color;
    ctx.beginPath();
    ctx.arc(x, y, 6, 0, Math.PI * 2);
    ctx.fill();
  }

  #outline(square, color, width) {
    const ctx = this.#ctx;
    const { x, y } = this.#corner(square);
    ctx.strokeStyle = color;
    ctx.lineWidth = width;
    ctx.strokeRect(x + width / 2 + 1, y + width / 2 + 1, TILE - width - 2, TILE - width - 2);
  }

  #ghost(square, color) {
    const ctx = this.#ctx;
    const { x, y } = this.#center(square);
    ctx.globalAlpha = 0.45;
    ctx.fillStyle = color;
    ctx.beginPath();
    ctx.arc(x, y - 3, TILE * 0.34, 0, Math.PI * 2);
    ctx.fill();
    ctx.globalAlpha = 1;
  }

  #hit({ at, label }) {
    const ctx = this.#ctx;
    const { x, y } = this.#corner(at);
    ctx.fillStyle = 'rgba(230,40,40,0.35)';
    ctx.fillRect(x, y, TILE, TILE);
    ctx.strokeStyle = '#ff4a4a';
    ctx.lineWidth = 2;
    ctx.strokeRect(x + 1, y + 1, TILE - 2, TILE - 2);
    ctx.font = 'bold 13px system-ui, sans-serif';
    ctx.textAlign = 'right';
    ctx.textBaseline = 'top';
    ctx.lineWidth = 3;
    ctx.strokeStyle = '#000000';
    ctx.strokeText(label, x + TILE - 4, y + 3);
    ctx.fillStyle = '#ffffff';
    ctx.fillText(label, x + TILE - 4, y + 3);
  }
}

function healthColor(fraction) {
  if (fraction > 0.6) return '#5fd35f';
  if (fraction > 0.3) return '#e5c23a';
  return '#e5483a';
}
