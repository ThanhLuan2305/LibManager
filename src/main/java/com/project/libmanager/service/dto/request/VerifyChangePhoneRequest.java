package com.project.libmanager.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@Schema(description = "Request data for verifying a phone number change with OTP")
public class VerifyChangePhoneRequest {
    @NotBlank(message = "NOT_BLANK")
    @Size(min = 10, max = 10, message = "PHONE_INVALID")
    @Schema(description = "Current phone number of the user", example = "0987654321")
    private String oldPhoneNumber;

    @NotBlank(message = "NOT_BLANK")
    @Size(min = 10, max = 10, message = "PHONE_INVALID")
    @Schema(description = "Desired new phone number", example = "0912345678")
    private String newPhoneNumber;

    @NotBlank(message = "NOT_BLANK")
    @Schema(description = "One-time password sent to the new phone", example = "654321")
    private String otp;
}
