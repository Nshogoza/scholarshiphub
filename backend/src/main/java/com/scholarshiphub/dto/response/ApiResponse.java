package com.scholarshiphub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/** Uniform envelope wrapping every successful (and error) API response body. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Instant timestamp
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, null, data, Instant.now());
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }

    public static <T> ApiResponse<T> message(String message) {
        return new ApiResponse<>(true, message, null, Instant.now());
    }
}
