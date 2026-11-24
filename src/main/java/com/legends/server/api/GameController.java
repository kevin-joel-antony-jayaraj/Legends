package com.legends.server.api;

import com.legends.server.game.GameService;
import com.legends.server.game.GameService.ActionOutcome;
import com.legends.server.game.GameService.CreatedGame;
import com.legends.server.view.GameView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * The game's HTTP API. See docs/API.md for examples.
 *
 * <p>Players identify themselves with the {@value #TOKEN_HEADER} header, using the token
 * they got when the game was created. The controller only translates HTTP to service
 * calls; it holds no game logic.
 */
@RestController
@RequestMapping("/api/games")
public class GameController {

    public static final String TOKEN_HEADER = "X-Player-Token";

    private final GameService games;

    public GameController(GameService games) {
        this.games = games;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedGame create(@Valid @RequestBody CreateGameRequest request) {
        return games.create(request.seed(), request.choices());
    }

    @GetMapping("/{gameId}")
    public GameView view(@PathVariable String gameId,
                         @RequestHeader(value = TOKEN_HEADER, required = false) String token) {
        return games.view(gameId, token);
    }

    @PostMapping("/{gameId}/actions")
    public ActionOutcome act(@PathVariable String gameId,
                             @RequestHeader(value = TOKEN_HEADER, required = false) String token,
                             @Valid @RequestBody ActionRequest request) {
        return games.act(gameId, token, request);
    }
}
