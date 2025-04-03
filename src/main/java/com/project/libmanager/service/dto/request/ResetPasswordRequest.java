package com.project.libmanager.service.dto.request;

import com.project.libmanager.validation.PasswordMatch;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@PasswordMatch()
@AllArgsConstructor
@Schema(description = "Request body for resetting user password")
public class ResetPasswordRequest {
    @NotBlank(message = "NOT_BLANK")
    @Schema(description = "Reset token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @NotBlank(message = "NOT_BLANK")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$", message = "INVALID_PASSWORD")
    @Schema(description = "New password, must be at least 8 characters with letters, numbers, and special characters", example = "NewPassword123!")
    private String newPassword;

    @NotBlank(message = "NOT_BLANK")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$", message = "INVALID_PASSWORD")
    @Schema(description = "Confirmation of the new password, must match newPassword", example = "NewPassword123!")
    private String confirmPassword;
}