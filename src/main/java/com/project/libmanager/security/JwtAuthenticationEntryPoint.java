package com.project.libmanager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.service.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Custom Spring Security AuthenticationEntryPoint for handling unauthenticated requests.
 * Returns a JSON response with error details when authentication fails.
 */
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Handles authentication failures by sending a standardized JSON error response.
     *
     * @param request       the {@link HttpServletRequest} that triggered the authentication failure
     * @param response      the {@link HttpServletResponse} to send the error response
     * @param authException the {@link AuthenticationException} that caused the failure
     * @throws IOException if writing to the response output stream fails
     * @implNote Sets HTTP status to 401 (Unauthorized), constructs an {@link ApiResponse}
     * with UNAUTHENTICATED error details, and serializes it to JSON.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // Define error code; fixed to UNAUTHENTICATED for all auth failures
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

        // Set response status; maps to HTTP 401 Unauthorized
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