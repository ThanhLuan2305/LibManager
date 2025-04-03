package com.project.libmanager.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
@Schema(description = "Request data for initiating a phone number change")
public class ChangePhoneRequest {
    @NotBlank(message = "NOT_BLANK")
    @Size(min = 10, max = 10, message = "PHONE_INVALID")
    @Schema(description = "Current phone number of the user", example = "0987654321")
    private String oldPhoneNumber;

    @NotBlank(message = "NOT_BLANK")
    @Size(min = 10, max = 10, message = "PHONE_INVALID")
    @Schema(description = "Desired new phone number", example = "0912345678")
    private String newPhoneNumber;
}
