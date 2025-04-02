package com.project.libmanager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.service.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom Spring Security {@link AccessDeniedHandler} for handling access denied scenarios.
 * Returns a JSON response with error details when a user lacks sufficient permissions.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * Handles access denied exceptions by sending a standardized JSON error response.
     *
     * @param request               the {@link HttpServletRequest} that triggered the access denial
     * @param response              the {@link HttpServletResponse} to send the error response
     * @param accessDeniedException the {@link AccessDeniedException} indicating insufficient permissions
     * @throws IOException if writing to the response output stream fails
     * @implNote Sets HTTP status to 403 (Forbidden), constructs an {@link ApiResponse}
     * with UNAUTHORIZED error details, and serializes it to JSON.
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        // Define error code; uses UNAUTHORIZED (likely intended as 403 Forbidden)
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        // Set response status; maps to HTTP 403 Forbidden (based on typical ErrorCode config)
        response.setStatus(errorCode.getStatusCode().value());
        // Set content type; ensures client expects JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Build error response; includes code and message, no data payload
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        // Initialize mapper; converts object to JSON string
        ObjectMapper objectMapper = new ObjectMapper();

        // Write JSON to response; sends error details to client
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        // Flush buffer; ensures response is fully sent
        response.flushBuffer();
    }
}