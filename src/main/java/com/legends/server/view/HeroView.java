package com.legends.server.view;

import com.legends.hero.HeroType;
import com.legends.hero.PawnStage;
import com.legends.model.Position;

/**
 * What every player can see about a hero.
 *
 * @param position   null once the hero has been defeated
 * @param pawnStage  null for heroes other than the Pawn
 * @param hasKnife   null for heroes other than the Pawn
 */
public record HeroView(
        int seat,
        String name,
        HeroType type,
        int halfHearts,
        int maxHalfHearts,
        boolean alive,
        Position position,
        boolean hasSuper,
        double superProgress,
        int poisonTurnsLeft,
        PawnStage pawnStage,
        Boolean hasKnife) {
}
