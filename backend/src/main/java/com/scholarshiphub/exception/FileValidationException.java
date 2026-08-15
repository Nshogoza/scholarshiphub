package com.scholarshiphub.exception;

public class FileValidationException extends BusinessException {

    public FileValidationException(String message) {
        super(ErrorCode.FILE_VALIDATION_FAILED, message);
    }
}
