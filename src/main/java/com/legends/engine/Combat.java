package com.legends.engine;

import com.legends.engine.event.GameEvent;
import com.legends.game.GameState;
import com.legends.hero.Hero;
import com.legends.hero.Pawn;
import com.legends.hero.PawnStage;
import com.legends.hero.PoisonStatus;
import com.legends.hero.attack.AttackOption;
import com.legends.hero.attack.Effect;
import com.legends.hero.attack.Hit;
import com.legends.model.Position;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Carries out an attack option. This is the one place damage is dealt and effects are
 * applied, always in the same order:
 * <ol>
 *   <li>Work out who is hit, pushed and poisoned, by where they stand right now.</li>
 *   <li>Move pieces: pushed pieces first (so the Knight's target makes room), then the attacker.</li>
 *   <li>Deal damage, and let the attacker's Super meter know.</li>
 *   <li>Apply the other effects: poison, the Keystone, bombs.</li>
 *   <li>Empty the meter if this was a Super.</li>
 *   <li>Trigger whatever moved heroes landed on (bombs, the Keystone).</li>
 * </ol>
 * Deciding the targets first means pieces moving around can never change who gets hit.
 */
final class Combat {

    private Combat() {
    }

    static void resolve(ActionContext context, Hero attacker, AttackOption option) {
        GameState game = context.game();
        int seat = attacker.owner().seat();
        boolean isSuper = game.isSuper(attacker, option.attackId());
        Optional<PawnStage> stageBefore = pawnStage(attacker);
        if (isSuper) {
            context.report(new GameEvent.SuperUsed(seat, option.attackId()));
        }

        // 1. Who is affected, decided before anything moves.
        Map<Hero, Integer> damage = new LinkedHashMap<>();
        for (Hit hit : option.hits()) {
            game.heroAt(hit.target()).ifPresent(target -> damage.merge(target, hit.halfHearts(), Integer::sum));
        }
        Map<Hero, Position> pushes = new LinkedHashMap<>();
        Map<Hero, Effect.Poison> poisons = new LinkedHashMap<>();
        for (Effect effect : option.effects()) {
            if (effect instanceof Effect.Push push) {
                game.heroAt(push.from()).ifPresent(hero -> pushes.put(hero, push.to()));
            } else if (effect instanceof Effect.Poison poison) {
                game.heroAt(poison.target()).ifPresent(hero -> poisons.put(hero, poison));
            }
        }

        // 2. Movement.
        List<Hero> moved = new ArrayList<>();
        pushes.forEach((hero, to) -> {
            moveTo(context, hero, to);
            moved.add(hero);
        });
        if (!option.endsAt().equals(game.positionOf(attacker).orElseThrow())) {
            moveTo(context, attacker, option.endsAt());
            moved.add(attacker);
        }

        // 3. Damage.
        damage.forEach((target, halfHearts) -> {
            int lost = target.takeDamage(halfHearts);
            if (lost > 0) {
                attacker.recordHitDealt(lost);
                context.report(new GameEvent.HeroDamaged(target.owner().seat(), seat, option.attackId(),
                        lost, target.halfHearts()));
            }
        });
        Optional<PawnStage> stageAfter = pawnStage(attacker);
        if (!stageAfter.equals(stageBefore)) {
            context.report(new GameEvent.PawnEvolved(seat, stageAfter.orElseThrow()));
        }

        // 4. Other effects.
        poisons.forEach((target, poison) -> {
            if (target.isAlive()) {
                target.applyPoison(new PoisonStatus(poison.turns(), poison.halfHeartsPerTurn(), seat));
                context.report(new GameEvent.HeroPoisoned(target.owner().seat(), poison.turns()));
            }
        });
        for (Effect effect : option.effects()) {
            switch (effect) {
                case Effect.DropKeystone drop -> {
                    if (attacker instanceof Pawn pawn) {
                        pawn.throwKnife();
                    }
                    game.dropKeystone(drop.at(), seat);
                    context.report(new GameEvent.KeystoneDropped(drop.at()));
                }
                case Effect.PlaceBomb bomb -> {
                    game.placeBomb(bomb.at(), seat);
                    context.report(new GameEvent.BombPlaced(seat));
                }
                case Effect.Push push -> {
                    // Already done in step 2.
                }
                case Effect.Poison poison -> {
                    // Already done above.
                }
            }
        }

        // 5. A Super empties the meter (after its own hits, so it can't recharge itself).
        if (isSuper) {
            attacker.consumeSuper();
        }

        // 6. Landing on bombs or the Keystone.
        for (Hero hero : moved) {
            if (hero.isAlive()) {
                Arrivals.arrive(context, hero);
            }
        }
    }

    private static void moveTo(ActionContext context, Hero hero, Position to) {
        Position from = context.game().positionOf(hero).orElseThrow();
        context.game().relocateHero(hero, to);
        context.report(new GameEvent.HeroMoved(hero.owner().seat(), from, to));
    }

    private static Optional<PawnStage> pawnStage(Hero hero) {
        return hero instanceof Pawn pawn ? Optional.of(pawn.stage()) : Optional.empty();
    }
}
