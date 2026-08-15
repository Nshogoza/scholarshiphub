package com.scholarshiphub.exception;

/** Thrown when a requested action is not valid given the entity's current state
 *  (e.g. submitting an already-submitted application, reviewing a draft). */
public class InvalidStateException extends BusinessException {

    public InvalidStateException(String message) {
        super(ErrorCode.INVALID_STATE, message);
    }
}
