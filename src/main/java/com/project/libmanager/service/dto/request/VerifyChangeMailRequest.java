package com.project.libmanager.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request data for verifying an email change with OTP")
public class VerifyChangeMailRequest {
    @NotBlank(message = "NOT_BLANK")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "EMAIL_INVALID")
    @Schema(description = "Current email address of the user", example = "old@example.com")
    private String oldEmail;

    @NotBlank(message = "NOT_BLANK")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "EMAIL_INVALID")
    @Schema(description = "Desired new email address", example = "new@example.com")
    private String newEmail;

    @NotBlank(message = "NOT_BLANK")
    @Schema(description = "One-time password sent to the new email", example = "123456")
    private String otp;
}
