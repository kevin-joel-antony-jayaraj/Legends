package com.legends.engine;

/**
 * Thrown when a player asks for something the rules don't allow. The message says why,
 * in words a player can read. Nothing in the game changes when this is thrown.
 */
public class IllegalActionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IllegalActionException(String reason) {
        super(reason);
    }
}
