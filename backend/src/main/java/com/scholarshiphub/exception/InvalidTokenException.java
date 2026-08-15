package com.scholarshiphub.exception;

public class InvalidTokenException extends BusinessException {

    public InvalidTokenException(String message) {
        super(ErrorCode.INVALID_TOKEN, message);
    }

    public static InvalidTokenException expired() {
        return new InvalidTokenException("Token has expired or already been used");
    }
}
