package com.legends.server.api;

import com.legends.engine.IllegalActionException;
import com.legends.server.game.GameNotFoundException;
import com.legends.server.game.NotAPlayerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Turns exceptions into clear HTTP errors with a JSON body like {@code {"error": "It's not your turn"}}.
 *
 * <ul>
 *   <li>400: the request is malformed or missing fields</li>
 *   <li>403: the player token is missing or wrong</li>
 *   <li>404: no such game</li>
 *   <li>409: the request is well formed, but the game rules don't allow it right now</li>
 * </ul>
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    public record ErrorResponse(String error) {
    }

    @ExceptionHandler(IllegalActionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponse illegalAction(IllegalActionException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(GameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse notFound(GameNotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(NotAPlayerException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ErrorResponse notAPlayer(NotAPlayerException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse badRequest(RuntimeException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse invalid(MethodArgumentNotValidException e) {
        String first = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .findFirst().orElse("Invalid request");
        return new ErrorResponse(first);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse unreadable(HttpMessageNotReadableException e) {
        return new ErrorResponse("Request body is not valid JSON for this endpoint");
    }
}
