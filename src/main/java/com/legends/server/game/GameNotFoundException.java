package com.legends.server.game;

/** No game has this id. Becomes HTTP 404. */
public class GameNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public GameNotFoundException(String gameId) {
        super("No game with id " + gameId);
    }
}
