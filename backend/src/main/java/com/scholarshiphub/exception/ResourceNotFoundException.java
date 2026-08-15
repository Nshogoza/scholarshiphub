package com.scholarshiphub.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String entityName, Object identifier) {
        super(ErrorCode.RESOURCE_NOT_FOUND, entityName + " not found with identifier: " + identifier);
    }

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
