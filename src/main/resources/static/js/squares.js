// Board squares are {row, col} objects, exactly as the server sends them.

/** True if two squares are the same. Either may be null. */
export function same(a, b) {
  return a != null && b != null && a.row === b.row && a.col === b.col;
}

/** A string key for a square, for Sets and Maps. */
export function key(square) {
  return `${square.row},${square.col}`;
}

/** The squares with duplicates removed, keeping the first of each. */
export function unique(squares) {
  const seen = new Set();
  return squares.filter((square) => {
    if (square == null || seen.has(key(square))) return false;
    seen.add(key(square));
    return true;
  });
}
