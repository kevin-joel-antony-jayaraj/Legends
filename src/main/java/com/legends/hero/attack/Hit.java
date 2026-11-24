package com.legends.hero.attack;

import com.legends.model.Position;

/**
 * One square taking damage from an attack.
 *
 * @param target     the square that is hit (whoever stands there takes the damage)
 * @param halfHearts how much damage, in half-hearts
 */
public record Hit(Position target, int halfHearts) {

    public Hit {
        if (halfHearts <= 0) {
            throw new IllegalArgumentException("A hit must deal damage, got " + halfHearts);
        }
    }
}
