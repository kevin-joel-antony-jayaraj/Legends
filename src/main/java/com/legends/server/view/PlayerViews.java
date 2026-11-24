package com.legends.server.view;

import com.legends.game.GameState;
import com.legends.game.Keystone;
import com.legends.hero.Hero;
import com.legends.hero.Pawn;
import com.legends.hero.PoisonStatus;
import com.legends.hero.attack.AttackOption;
import com.legends.hero.attack.Effect;
import com.legends.model.Position;
import com.legends.model.TileType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Builds the {@link GameView} a given player is allowed to see.
 *
 * <p>Read this class as the server's privacy rule: anything not copied here never reaches
 * a player. In particular {@code GameState.hiddenBombs()} is never read.
 */
public final class PlayerViews {

    private PlayerViews() {
    }

    public static GameView forSeat(String gameId, GameState game, int seat) {
        boolean yourTurn = !game.isOver() && game.currentSeat() == seat;
        Hero you = game.heroInSeat(seat);

        List<Position> moves = yourTurn
                ? game.legalMoves(you).stream()
                        .sorted(Comparator.comparingInt(Position::row).thenComparingInt(Position::col))
                        .toList()
                : List.of();
        List<AttackChoice> attacks = yourTurn
                ? game.attackOptions(you).stream().map(option -> choice(game, you, option)).toList()
                : List.of();

        return new GameView(
                gameId,
                seat,
                tiles(game),
                game.heroes().stream().map(hero -> hero(game, hero)).toList(),
                game.keystone().map(Keystone::at).orElse(null),
                game.currentSeat(),
                game.actionsLeft(),
                game.isOver(),
                game.winner().map(hero -> hero.owner().seat()).orElse(null),
                moves,
                attacks);
    }

    private static List<List<TileType>> tiles(GameState game) {
        List<List<TileType>> rows = new ArrayList<>();
        for (int row = 0; row < game.board().rows(); row++) {
            List<TileType> tiles = new ArrayList<>();
            for (int col = 0; col < game.board().cols(); col++) {
                tiles.add(game.board().tileAt(new Position(row, col)));
            }
            rows.add(List.copyOf(tiles));
        }
        return List.copyOf(rows);
    }

    private static HeroView hero(GameState game, Hero hero) {
        Pawn pawn = hero instanceof Pawn p ? p : null;
        return new HeroView(
                hero.owner().seat(),
                hero.owner().name(),
                hero.type(),
                hero.halfHearts(),
                hero.maxHalfHearts(),
                hero.isAlive(),
                game.positionOf(hero).orElse(null),
                hero.hasSuper(),
                hero.superProgress(),
                hero.poison().map(PoisonStatus::turnsLeft).orElse(0),
                pawn == null ? null : pawn.stage(),
                pawn == null ? null : pawn.hasKnife());
    }

    /** Only pushes are copied. Other effects stay on the server; a placed bomb's square is secret. */
    private static List<AttackChoice.PushView> pushes(AttackOption option) {
        return option.effects().stream()
                .filter(Effect.Push.class::isInstance)
                .map(Effect.Push.class::cast)
                .map(push -> new AttackChoice.PushView(push.from(), push.to()))
                .toList();
    }

    private static AttackChoice choice(GameState game, Hero hero, AttackOption option) {
        return new AttackChoice(
                option.attackId(),
                game.isSuper(hero, option.attackId()),
                option.aim(),
                option.endsAt(),
                option.firstPushDestination().orElse(null),
                option.hits(),
                pushes(option));
    }
}
