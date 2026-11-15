package com.eventproof.exception;

/** Unchecked exception for invalid values rejected by Event's existing rules. */
public class InvalidEventDataException extends IllegalArgumentException {
    public InvalidEventDataException(String message) {
        super(message);
    }
}
