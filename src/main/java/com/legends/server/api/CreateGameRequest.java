package com.legends.server.api;

import com.legends.game.PlayerChoice;
import com.legends.hero.HeroType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Body of {@code POST /api/games}.
 *
 * <p>Example: {@code {"seed": 42, "players": [{"name": "Ana", "hero": "ARCHER"}, ...]}}.
 * Leave out the seed for a random board.
 */
public record CreateGameRequest(Long seed, @NotNull @Size(min = 2, max = 4) List<@Valid Player> players) {

    public record Player(@NotBlank String name, @NotNull HeroType hero) {
    }

    List<PlayerChoice> choices() {
        return players.stream().map(p -> new PlayerChoice(p.name(), p.hero())).toList();
    }
}
