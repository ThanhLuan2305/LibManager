package com.project.libmanager.service.dto.response;

import com.project.libmanager.constant.VerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

/**
 * Data Transfer Object for user information response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Detailed information of the user")
public class UserResponse {
    @Schema(description = "ID of the user", example = "1")
    private Long id;

    @Schema(description = "Email address of the user", example = "user@example.com")
    private String email;

    @Schema(description = "Phone number of the user", example = "0987654321")
    private String phoneNumber;

    @Schema(description = "Full name of the user", example = "John Doe")
    private String fullName;

    @Schema(description = "Birth date of the user", example = "1990-01-01T00:00:00Z")
    private Instant birthDate;

    @Schema(description = "Verification status of the user", example = "VERIFIED")
    private VerificationStatus verificationStatus;

    @Schema(description = "Indicates if the user is deleted", example = "false")
    private boolean deleted;

    @Schema(description = "Count of late book returns by the user", example = "2")
    private int lateReturnCount;

    @Schema(description = "Set of roles assigned to the user")
    private Set<RoleResponse> roles;

    @Schema(description = "Timestamp when the user was created", example = "2023-01-01T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the user was last updated", example = "2023-06-01T12:00:00Z")
    private Instant updatedAt;

    @Schema(description = "Username of the creator of the user record", example = "admin")
    private String createdBy;

    @Schema(description = "Username of the last updater of the user record", example = "admin")
    private String updatedBy;
}