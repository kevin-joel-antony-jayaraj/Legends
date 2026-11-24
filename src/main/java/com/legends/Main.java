package com.legends;

import com.legends.board.BoardGenerator;
import com.legends.board.BoardRenderer;
import com.legends.board.GeneratedBoard;
import com.legends.model.Position;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Console entry point. Generates a random board and prints it.
 *
 * <p>Usage: {@code java com.legends.Main [seed]}. Pass a seed to get the same board again.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : new Random().nextLong();
        GeneratedBoard generated = new BoardGenerator().generate(seed);

        Map<Position, Character> starts = new HashMap<>();
        generated.starts().values().forEach(p -> starts.put(p, 'S'));

        System.out.println("Seed: " + generated.seed() + "  (attempts: " + generated.attempts() + ")");
        System.out.println("Corners: " + generated.cornerTypes());
        System.out.print(BoardRenderer.render(generated.board(), starts));
    }
}
