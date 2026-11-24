package com.legends.engine;

import com.legends.engine.event.GameEvent;
import com.legends.game.Bomb;
import com.legends.game.GameState;
import com.legends.game.Keystone;
import com.legends.hero.Hero;
import com.legends.hero.Knight;
import com.legends.hero.Pawn;
import com.legends.hero.PawnStage;
import com.legends.model.Position;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * What happens when a hero arrives on a square, however it got there: walking, jumping,
 * being pushed or pulled. Bombs go off first, then the Keystone.
 */
final class Arrivals {

    private Arrivals() {
    }

    static void arrive(ActionContext context, Hero hero) {
        GameState game = context.game();
        Position at = game.positionOf(hero).orElse(null);
        if (at == null) {
            return;
        }

        int seat = hero.owner().seat();
        List<Bomb> enemyBombsHere = game.hiddenBombs().stream()
                .filter(bomb -> bomb.at().equals(at) && bomb.ownerSeat() != seat)
                .toList();
        if (!enemyBombsHere.isEmpty()) {
            detonate(context, enemyBombsHere);
        }

        if (hero.isAlive()) {
            game.keystone().filter(keystone -> keystone.at().equals(at))
                    .ifPresent(keystone -> touchKeystone(context, hero, keystone));
        }
    }

    /**
     * Sets off bombs. Each blast covers the 3×3 square around its bomb: it damages every
     * hero there except the bomb's owner, and sets off every other bomb there.
     */
    private static void detonate(ActionContext context, List<Bomb> first) {
        GameState game = context.game();
        Deque<Bomb> toExplode = new ArrayDeque<>(first);
        while (!toExplode.isEmpty()) {
            Bomb bomb = toExplode.poll();
            if (!game.removeBomb(bomb)) {
                continue; // already went off as part of this chain
            }
            context.report(new GameEvent.BombExploded(bomb.at()));

            for (Hero victim : game.livingHeroes()) {
                boolean inBlast = game.positionOf(victim)
                        .map(p -> p.kingDistance(bomb.at()) <= Knight.BOMB_BLAST_RADIUS)
                        .orElse(false);
                if (inBlast && victim.owner().seat() != bomb.ownerSeat()) {
                    int lost = victim.takeDamage(Knight.BOMB_DAMAGE);
                    if (lost > 0) {
                        context.report(new GameEvent.HeroDamaged(victim.owner().seat(), bomb.ownerSeat(), "bomb",
                                lost, victim.halfHearts()));
                    }
                }
            }

            for (Bomb other : game.hiddenBombs()) {
                if (other.at().kingDistance(bomb.at()) <= Knight.BOMB_BLAST_RADIUS) {
                    toExplode.add(other);
                }
            }
        }
    }

    /**
     * The Pawn that threw the knife becomes a Queen, teleports, and the Keystone is gone.
     * Anyone else is fully healed and the Keystone stays (no cooldown).
     */
    private static void touchKeystone(ActionContext context, Hero hero, Keystone keystone) {
        GameState game = context.game();
        int seat = hero.owner().seat();

        if (seat == keystone.ownerSeat() && hero instanceof Pawn pawn && pawn.stage() != PawnStage.QUEEN) {
            game.removeKeystone();
            context.report(new GameEvent.KeystoneClaimed(seat));
            pawn.becomeQueen();
            context.report(new GameEvent.PawnEvolved(seat, PawnStage.QUEEN));
            teleport(context, pawn);
            return;
        }

        int missing = hero.maxHalfHearts() - hero.halfHearts();
        if (missing > 0) {
            hero.healFull();
            context.report(new GameEvent.HeroHealed(seat, missing));
        }
    }

    /**
     * Sends the new Queen to a random free square (docs/HEROES.md: anywhere except rivers
     * and blocked tiles). It can land on a hidden bomb, which then goes off.
     */
    private static void teleport(ActionContext context, Hero hero) {
        GameState game = context.game();
        List<Position> choices = new ArrayList<>();
        for (int row = 0; row < game.board().rows(); row++) {
            for (int col = 0; col < game.board().cols(); col++) {
                Position square = new Position(row, col);
                if (game.isFreeToEnter(square)) {
                    choices.add(square);
                }
            }
        }
        if (choices.isEmpty()) {
            return;
        }
        Position from = game.positionOf(hero).orElseThrow();
        Position to = choices.get(context.random().nextInt(choices.size()));
        game.relocateHero(hero, to);
        context.report(new GameEvent.HeroMoved(hero.owner().seat(), from, to));
        arrive(context, hero); // the Keystone is already gone, so this only checks for bombs
    }
}
