package com.legends.engine;

import com.legends.engine.event.GameEvent;
import com.legends.game.GameState;
import com.legends.hero.Hero;
import com.legends.hero.Pawn;
import com.legends.hero.PawnStage;
import com.legends.hero.attack.AttackOption;
import com.legends.hero.attack.Hit;
import com.legends.model.Position;
import java.util.List;
import java.util.Optional;

/**
 * Applies an attack option to the game: moves the attacker if the attack says so, then
 * deals each hit and lets the attacker's Super meter know.
 *
 * <p>This is the one place damage is dealt, so rules like "a hit only counts if it did
 * damage" live in exactly one spot.
 */
final class Combat {

    private Combat() {
    }

    static void resolve(GameState game, Hero attacker, AttackOption option, List<GameEvent> events) {
        int attackerSeat = attacker.owner().seat();
        Optional<PawnStage> stageBefore = pawnStage(attacker);

        Position from = game.positionOf(attacker).orElseThrow();
        if (!option.endsAt().equals(from)) {
            game.relocateHero(attacker, option.endsAt());
            events.add(new GameEvent.HeroMoved(attackerSeat, from, option.endsAt()));
        }

        for (Hit hit : option.hits()) {
            game.heroAt(hit.target()).ifPresent(target -> {
                int lost = target.takeDamage(hit.halfHearts());
                if (lost > 0) {
                    attacker.recordHitDealt(lost);
                    events.add(new GameEvent.HeroDamaged(target.owner().seat(), attackerSeat,
                            option.attackId(), lost, target.halfHearts()));
                }
            });
        }

        Optional<PawnStage> stageAfter = pawnStage(attacker);
        if (!stageAfter.equals(stageBefore)) {
            events.add(new GameEvent.PawnEvolved(attackerSeat, stageAfter.orElseThrow()));
        }
    }

    private static Optional<PawnStage> pawnStage(Hero hero) {
        return hero instanceof Pawn pawn ? Optional.of(pawn.stage()) : Optional.empty();
    }
}
