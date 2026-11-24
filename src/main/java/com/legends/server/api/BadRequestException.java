package com.legends.server.api;

/** The request is missing something or is badly formed. Becomes HTTP 400. */
public class BadRequestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BadRequestException(String message) {
        super(message);
    }
}
