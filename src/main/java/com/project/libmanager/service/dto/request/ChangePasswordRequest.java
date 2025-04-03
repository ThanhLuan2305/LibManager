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
@Schema(description = "Request data for changing user password")
public class ChangePasswordRequest {
    @Schema(description = "Current password of the user", example = "oldPass@123")
    @NotBlank(message = "NOT_BLANK")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$", message = "INVALID_PASSWORD")
    private String oldPassword;

    @NotBlank(message = "NOT_BLANK")
    @Schema(description = "Desired new password", example = "newPass@123")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$", message = "INVALID_PASSWORD")
    private String newPassword;

    @NotBlank(message = "NOT_BLANK")
    @Schema(description = "Confirm new password", example = "newPass@123")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$", message = "INVALID_PASSWORD")
    private String confirmPassword;
}
