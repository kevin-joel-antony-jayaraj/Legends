package com.legends;

import com.legends.cli.ConsoleView;
import com.legends.game.GameSetup;
import com.legends.game.GameState;
import com.legends.game.PlayerChoice;
import com.legends.hero.Hero;
import com.legends.hero.HeroType;
import java.util.List;
import java.util.Random;

/**
 * Console entry point. Sets up a 4-player game and prints it, plus where the current
 * hero could move.
 *
 * <p>Usage: {@code java com.legends.Main [seed]}. Pass a seed to get the same game again.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : new Random().nextLong();

        GameState game = new GameSetup().newGame(seed, List.of(
                new PlayerChoice("Ana", HeroType.ARCHER),
                new PlayerChoice("Ben", HeroType.KNIGHT),
                new PlayerChoice("Cleo", HeroType.CASTLE),
                new PlayerChoice("Dev", HeroType.PAWN)));

        System.out.println("Seed: " + game.seed());
        System.out.print(ConsoleView.render(game));

        Hero current = game.currentHero();
        System.out.println(current.type() + " can move to: " + game.legalMoves(current));
    }
}
