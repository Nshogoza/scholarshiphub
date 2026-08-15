package com.scholarshiphub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        boolean success,
        String errorCode,
        String message,
        String path,
        Instant timestamp,
        List<FieldErrorDetail> fieldErrors
) {

    public static ErrorResponse of(String errorCode, String message, String path) {
        return new ErrorResponse(false, errorCode, message, path, Instant.now(), null);
    }

    public static ErrorResponse ofValidation(String errorCode, String message, String path,
                                              List<FieldErrorDetail> fieldErrors) {
        return new ErrorResponse(false, errorCode, message, path, Instant.now(), fieldErrors);
    }

    public record FieldErrorDetail(String field, String message) {
    }
}
