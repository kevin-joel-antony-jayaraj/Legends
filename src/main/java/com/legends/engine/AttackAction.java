package com.legends.engine;

import com.legends.engine.event.GameEvent;
import com.legends.game.GameState;
import com.legends.hero.Hero;
import com.legends.hero.attack.AttackOption;
import com.legends.model.Position;
import java.util.List;
import java.util.Optional;

/**
 * Use one of the current hero's attacks. Costs 1 action.
 *
 * <p>The request names the attack and the square aimed at. The engine works out the
 * attack's options itself and only accepts the request if it matches one, so a client
 * can never invent damage or targets.
 *
 * @param attackId which attack, for example "diagonal-shot"
 * @param aim      the square aimed at (for the Pawn's queen-lines, the Pawn's own square)
 * @param landing  where the attacker should end up, only needed when an attack can land in
 *                 more than one place (the Knight's Check); otherwise {@code null}
 */
public record AttackAction(int seat, String attackId, Position aim, Position landing) implements Action {

    /** For attacks where the attacker doesn't move, or can only land in one place. */
    public AttackAction(int seat, String attackId, Position aim) {
        this(seat, attackId, aim, null);
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
            return Optional.of("That " + attackId + " can land in more than one place; say where to land");
        }
        return Optional.empty();
    }

    @Override
    public void apply(GameState game, List<GameEvent> events) {
        Combat.resolve(game, game.heroInSeat(seat), matchingOptions(game).get(0), events);
    }

    private List<AttackOption> matchingOptions(GameState game) {
        return game.attackOptions(game.heroInSeat(seat)).stream()
                .filter(option -> option.attackId().equals(attackId) && option.aim().equals(aim))
                .filter(option -> landing == null || option.endsAt().equals(landing))
                .toList();
    }
}
