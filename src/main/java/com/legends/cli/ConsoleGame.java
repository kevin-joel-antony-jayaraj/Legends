package com.legends.cli;

import com.legends.engine.Action;
import com.legends.engine.AttackAction;
import com.legends.engine.EndTurnAction;
import com.legends.engine.GameEngine;
import com.legends.engine.IllegalActionException;
import com.legends.engine.MoveAction;
import com.legends.engine.event.GameEvent;
import com.legends.game.GameState;
import com.legends.hero.Hearts;
import com.legends.hero.Hero;
import com.legends.hero.attack.AttackOption;
import com.legends.hero.attack.Hit;
import com.legends.model.Position;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A hot-seat game in the terminal: players take turns typing commands on one keyboard.
 *
 * <p>This class only translates text into {@link Action}s and prints results. Every rule
 * lives in the engine, so a web front end can replace this class without touching the game.
 */
public final class ConsoleGame {

    private static final String HELP = """
            Commands:
              moves             list the squares you can move to
              move ROW COL      move there (costs 1 action)
              attacks           list the attacks you can make, numbered (SUPER when charged)
              attack N          use attack number N from the list (costs 1 action)
              aim NAME ROW COL  use an attack that can target many squares, like: aim bomb 5 6
              end               end your turn early
              board             show the board again
              help              show this help
              quit              leave the game""";

    /** Above this many options, an attack is typed with 'aim' instead of listed one by one. */
    private static final int MANY_TARGETS = 16;

    private final GameEngine engine;
    private final BufferedReader in;
    private final PrintStream out;

    public ConsoleGame(GameEngine engine, BufferedReader in, PrintStream out) {
        this.engine = engine;
        this.in = in;
        this.out = out;
    }

    /** Runs until the game ends, a player types quit, or input runs out. */
    public void run() throws IOException {
        GameState game = engine.game();
        out.println(HELP);
        out.println();
        out.print(ConsoleView.render(game));

        while (!game.isOver()) {
            out.print("\n" + EventText.name(game, game.currentSeat()) + "> ");
            out.flush();
            String line = in.readLine();
            if (line == null || line.trim().equalsIgnoreCase("quit")) {
                out.println("Bye!");
                return;
            }
            handle(line.trim().split("\\s+"));
        }
    }

    private void handle(String[] words) {
        GameState game = engine.game();
        Hero hero = game.currentHero();
        int seat = game.currentSeat();
        try {
            switch (words[0].toLowerCase()) {
                case "", "help" -> out.println(HELP);
                case "board" -> out.print(ConsoleView.render(game));
                case "moves" -> out.println("You can move to: " + squares(List.copyOf(game.legalMoves(hero))));
                case "attacks" -> listAttacks(game, hero);
                case "move" -> perform(new MoveAction(seat, new Position(number(words, 1), number(words, 2))));
                case "attack" -> {
                    List<AttackOption> options = numberedOptions(game.attackOptions(hero));
                    int choice = number(words, 1);
                    if (choice < 1 || choice > options.size()) {
                        out.println("No attack number " + choice + ". Type 'attacks' to see the list.");
                        return;
                    }
                    perform(AttackAction.choosing(seat, options.get(choice - 1)));
                }
                case "aim" -> perform(new AttackAction(seat, words[1],
                        new Position(number(words, 2), number(words, 3))));
                case "end" -> perform(new EndTurnAction(seat));
                default -> out.println("Unknown command '" + words[0] + "'. Type 'help'.");
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            out.println("That command needs numbers, for example: move 3 4");
        }
    }

    private void perform(Action action) {
        try {
            List<GameEvent> events = engine.perform(action);
            for (GameEvent event : events) {
                out.println("  " + EventText.describe(event, engine.game()));
            }
            out.println();
            out.print(ConsoleView.render(engine.game()));
        } catch (IllegalActionException e) {
            out.println("Not allowed: " + e.getMessage());
        }
    }

    private void listAttacks(GameState game, Hero hero) {
        List<AttackOption> all = game.attackOptions(hero);
        if (all.isEmpty()) {
            out.println("No attacks available from here.");
            return;
        }
        List<AttackOption> numbered = numberedOptions(all);
        for (int i = 0; i < numbered.size(); i++) {
            AttackOption option = numbered.get(i);
            String label = (game.isSuper(hero, option.attackId()) ? "SUPER " : "") + option.attackId();
            out.printf("  %d) %s at %s%s%n", i + 1, label, EventText.square(option.aim()), describe(game, option));
        }
        all.stream().map(AttackOption::attackId).distinct()
                .filter(id -> isManyTargeted(all, id))
                .forEach(id -> out.printf("  %s%s: %d possible squares. Type 'aim %s ROW COL'.%n",
                        game.isSuper(hero, id) ? "SUPER " : "", id, count(all, id), id));
    }

    /** What an option does, in words: landing, pushes and damage. */
    private static String describe(GameState game, AttackOption option) {
        List<String> parts = new ArrayList<>();
        game.positionOf(game.currentHero())
                .filter(from -> !from.equals(option.endsAt()))
                .ifPresent(from -> parts.add("land on " + EventText.square(option.endsAt())));
        option.firstPushDestination().ifPresent(to -> parts.add("knock to " + EventText.square(to)));
        for (Hit hit : option.hits()) {
            String who = game.heroAt(hit.target()).map(h -> h.owner().name()).orElse("?");
            parts.add(who + " " + Hearts.format(hit.halfHearts()) + "h");
        }
        return parts.isEmpty() ? "" : ": " + String.join(", ", parts);
    }

    /** Options shown with numbers. Attacks that can target a huge number of squares (the bomb) are typed with 'aim' instead. */
    private static List<AttackOption> numberedOptions(List<AttackOption> all) {
        return all.stream().filter(option -> !isManyTargeted(all, option.attackId())).toList();
    }

    private static boolean isManyTargeted(List<AttackOption> all, String attackId) {
        return count(all, attackId) > MANY_TARGETS;
    }

    private static long count(List<AttackOption> all, String attackId) {
        return all.stream().filter(option -> option.attackId().equals(attackId)).count();
    }

    private static List<String> squares(List<Position> positions) {
        return positions.stream().map(EventText::square).toList();
    }

    private static int number(String[] words, int index) {
        return Integer.parseInt(words[index]);
    }
}
