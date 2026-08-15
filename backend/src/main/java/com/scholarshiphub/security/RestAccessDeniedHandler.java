package com.scholarshiphub.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarshiphub.dto.response.ErrorResponse;
import com.scholarshiphub.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** Emits a JSON {@link ErrorResponse} when an authenticated user's role
 *  lacks permission for the requested endpoint (RBAC denial). */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = ErrorResponse.of(ErrorCode.ACCESS_DENIED.name(),
                "You do not have permission to perform this action", request.getRequestURI());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
