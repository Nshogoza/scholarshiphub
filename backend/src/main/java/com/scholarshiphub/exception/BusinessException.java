package com.scholarshiphub.exception;

import lombok.Getter;

/** Base type for all deliberately-thrown, client-facing application errors. */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
