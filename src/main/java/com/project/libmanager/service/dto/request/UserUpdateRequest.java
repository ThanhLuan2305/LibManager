package com.project.libmanager.service.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.libmanager.constant.VerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Schema(description = "Request body for updating an existing user")
public class UserUpdateRequest {
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$", message = "INVALID_PASSWORD")
    @Schema(description = "User's new password, must be at least 8 characters with letters, numbers, and special characters", example = "NewPassword123!")
    private String password;

    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    @Schema(description = "User's full name", example = "John Doe")
    private String fullName;

    @Past(message = "BIRTH_DATE_MUST_BE_IN_PAST")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'HH:mm:ss.SSSX", timezone = "UTC")
    @Schema(description = "User's birth date, must be in the past", example = "01-01-1990T00:00:00.000Z")
    private Instant birthDate;

    @Schema(description = "Verification status of the user", example = "VERIFIED")
    private VerificationStatus verificationStatus;

    @Schema(description = "List of roles assigned to the user", example = "[\"USER\", \"ADMIN\"]")
    private List<String> listRole;

    @Schema(description = "Whether the user is marked as deleted", example = "false")
    private boolean deleted;

    @Schema(description = "Number of late returns by the user", example = "0")
    private int lateReturnCount;
}