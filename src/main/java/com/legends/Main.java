package com.legends;

import com.legends.cli.ConsoleGame;
import com.legends.engine.GameEngine;
import com.legends.game.GameSetup;
import com.legends.game.GameState;
import com.legends.game.PlayerChoice;
import com.legends.hero.HeroType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

/**
 * Console entry point: a 4-player hot-seat game in the terminal.
 *
 * <p>Usage: {@code java com.legends.Main [seed]}. Pass a seed to replay the same board.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : new Random().nextLong();

        GameState game = new GameSetup().newGame(seed, List.of(
                new PlayerChoice("Ana", HeroType.ARCHER),
                new PlayerChoice("Ben", HeroType.KNIGHT),
                new PlayerChoice("Cleo", HeroType.CASTLE),
                new PlayerChoice("Dev", HeroType.PAWN)));

        System.out.println("LEGENDS  (seed " + seed + ")");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        new ConsoleGame(new GameEngine(game), in, System.out).run();
    }
}
