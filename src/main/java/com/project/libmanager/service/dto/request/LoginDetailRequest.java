package com.project.libmanager.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "Request body for login details")
public class LoginDetailRequest {

    @Schema(description = "JWT token ID (jti)", example = "123e4567-e89b-12d3-a456-426614174000")
    private String jti;

    @Schema(description = "Whether the login session is enabled", example = "true")
    private boolean enabled;

    @Schema(description = "Expiration time of the login session", example = "2025-04-02T12:00:00.000Z")
    private Instant expiredAt;

    @Schema(description = "User's email address", example = "user@example.com")
    private String email;
}