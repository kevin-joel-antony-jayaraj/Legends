package com.legends.cli;

import com.legends.board.BoardRenderer;
import com.legends.game.GameState;
import com.legends.hero.Hearts;
import com.legends.hero.Hero;
import com.legends.hero.HeroType;
import com.legends.hero.Pawn;
import com.legends.model.Position;
import java.util.HashMap;
import java.util.Map;

/**
 * Draws a game for the console: the board with hero letters, then a status line per hero.
 *
 * <p>A view only. It reads the game state and never changes it.
 */
public final class ConsoleView {

    /** Board symbol for the Keystone (shown when nobody is standing on it). */
    static final char KEYSTONE = '*';

    private ConsoleView() {
    }

    public static String render(GameState game) {
        Map<Position, Character> overlay = new HashMap<>();
        // Bombs are hidden, so they are never drawn. The Keystone is public.
        game.keystone().ifPresent(keystone -> overlay.put(keystone.at(), KEYSTONE));
        for (Hero hero : game.livingHeroes()) {
            game.positionOf(hero).ifPresent(p -> overlay.put(p, letterFor(hero.type())));
        }

        StringBuilder out = new StringBuilder(BoardRenderer.render(game.board(), overlay));
        out.append('\n');
        for (Hero hero : game.heroes()) {
            out.append(hero == game.currentHero() ? "> " : "  ")
                    .append(letterFor(hero.type())).append("  ")
                    .append(String.format("%-8s", hero.owner().name()))
                    .append(String.format("%-7s", hero.type()))
                    .append(Hearts.format(hero.halfHearts())).append('/').append(Hearts.format(hero.maxHalfHearts()))
                    .append(" hearts");
            if (hero.hasSuper()) {
                out.append(String.format("   super %3d%%", Math.round(hero.superProgress() * 100)));
            }
            if (hero instanceof Pawn pawn) {
                out.append("   ").append(pawn.stage()).append(pawn.hasKnife() ? ", has knife" : "");
            }
            hero.poison().ifPresent(p -> out.append("   poisoned (").append(p.turnsLeft()).append(" turns)"));
            if (!hero.isAlive()) {
                out.append("   (out)");
            }
            out.append('\n');
        }
        game.keystone().ifPresent(k -> out.append("\n").append(KEYSTONE).append(" = Keystone at ").append(k.at()));
        out.append("\nTurn: ").append(game.currentHero().owner().name())
                .append(", ").append(game.actionsLeft()).append(" actions left\n");
        return out.toString();
    }

    static char letterFor(HeroType type) {
        return switch (type) {
            case ARCHER -> 'A';
            case KNIGHT -> 'K';
            case CASTLE -> 'C';
            case PAWN -> 'W'; // 'P' is already the spawn-area tile
        };
    }
}
