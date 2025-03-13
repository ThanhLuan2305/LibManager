package com.project.libmanager.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class ChangePhoneRequest {
    @NotBlank(message = "NOT_BLANK")
    @Size(min = 10, max = 10, message = "PHONE_INVALID")
    private String oldPhoneNumber;

    @NotBlank(message = "NOT_BLANK")
    @Size(min = 10, max = 10, message = "PHONE_INVALID")
    private String newPhoneNumber;
}
