package com.legends.server.game;

/** The player token is missing or doesn't belong to this game. Becomes HTTP 403. */
public class NotAPlayerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NotAPlayerException() {
        super("That player token isn't part of this game");
    }
}
