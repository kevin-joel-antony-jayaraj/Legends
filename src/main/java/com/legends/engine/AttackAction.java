package com.legends.engine;

import com.legends.game.GameState;
import com.legends.hero.attack.AttackOption;
import com.legends.hero.attack.Effect;
import com.legends.model.Position;
import java.util.List;
import java.util.Optional;

/**
 * Use one of the current hero's attacks (or its Super). Costs 1 action.
 *
 * <p>The request names the attack and the square aimed at. The engine works out the
 * attack's options itself and only accepts the request if it matches exactly one, so a
 * client can never invent damage, targets or pushes.
 *
 * @param attackId which attack, for example "diagonal-shot"
 * @param aim      the square aimed at (for attacks that hit everything around, the hero's own square)
 * @param landing  where the attacker should end up; only needed when that's a choice (Knight's Check)
 * @param pushTo   where the target should be pushed; only needed when that's a choice (Knight's Check)
 */
public record AttackAction(int seat, String attackId, Position aim, Position landing, Position pushTo)
        implements Action {

    /** Shown when the Knight picks a square that already has a bomb. The Knight can't see its bombs either. */
    public static final String BOMB_ALREADY_THERE = "A bomb already exists on that square";

    /** For attacks with only one way to hit the aimed square. */
    public AttackAction(int seat, String attackId, Position aim) {
        this(seat, attackId, aim, null, null);
    }

    /** Picks an exact option, for example one chosen from {@code GameState.attackOptions}. */
    public static AttackAction choosing(int seat, AttackOption option) {
        return new AttackAction(seat, option.attackId(), option.aim(), option.endsAt(),
                option.firstPushDestination().orElse(null));
    }

    @Override
    public int cost() {
        return 1;
    }

    @Override
    public Optional<String> problem(GameState game) {
        List<AttackOption> matches = matchingOptions(game);
        if (matches.isEmpty()) {
            return Optional.of(game.heroInSeat(seat).type() + " can't use " + attackId + " on " + aim);
        }
        if (matches.size() > 1) {
            return Optional.of("That " + attackId + " can be done more than one way; say where to land and push");
        }
        if (placesBombOnABomb(game, matches.get(0))) {
            return Optional.of(BOMB_ALREADY_THERE);
        }
        return Optional.empty();
    }

    @Override
    public void apply(ActionContext context) {
        GameState game = context.game();
        Combat.resolve(context, game.heroInSeat(seat), matchingOptions(game).get(0));
    }

    /** Attacks can't see bombs, so this one rule is checked here, where the game state is known. */
    private static boolean placesBombOnABomb(GameState game, AttackOption option) {
        return option.effects().stream()
                .anyMatch(effect -> effect instanceof Effect.PlaceBomb place && game.hasBomb(place.at()));
    }

    private List<AttackOption> matchingOptions(GameState game) {
        return game.attackOptions(game.heroInSeat(seat)).stream()
                .filter(option -> option.attackId().equals(attackId) && option.aim().equals(aim))
                .filter(option -> landing == null || option.endsAt().equals(landing))
                .filter(option -> pushTo == null || option.firstPushDestination().filter(pushTo::equals).isPresent())
                .toList();
    }
}
