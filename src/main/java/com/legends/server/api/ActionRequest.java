package com.legends.server.api;

import com.legends.engine.Action;
import com.legends.engine.AttackAction;
import com.legends.engine.EndTurnAction;
import com.legends.engine.MoveAction;
import com.legends.model.Position;
import com.legends.server.game.GameService;
import jakarta.validation.constraints.NotNull;

/**
 * Body of {@code POST /api/games/{gameId}/actions}.
 *
 * <p>Examples:
 * <pre>
 * {"type": "MOVE", "to": {"row": 2, "col": 2}}
 * {"type": "ATTACK", "attackId": "diagonal-shot", "aim": {"row": 3, "col": 3}}
 * {"type": "ATTACK", "attackId": "check", "aim": {...}, "landing": {...}, "pushTo": {...}}
 * {"type": "END_TURN"}
 * </pre>
 * The seat isn't in the body: the server takes it from the player's token, so nobody can
 * act for someone else.
 */
public record ActionRequest(@NotNull Type type, Position to, String attackId, Position aim, Position landing,
                            Position pushTo) implements GameService.ActionFactory {

    public enum Type { MOVE, ATTACK, END_TURN }

    @Override
    public Action toAction(int seat) {
        return switch (type) {
            case MOVE -> new MoveAction(seat, require(to, "to"));
            case ATTACK -> new AttackAction(seat, require(attackId, "attackId"), require(aim, "aim"), landing, pushTo);
            case END_TURN -> new EndTurnAction(seat);
        };
    }

    private static <T> T require(T value, String field) {
        if (value == null) {
            throw new BadRequestException("'" + field + "' is required for this action");
        }
        return value;
    }
}
