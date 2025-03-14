package com.project.LibManager.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeMailRequest {
    @NotBlank(message = "NOT_BLANK")
    @Email(message = "EMAIL_INVALID")
    private String newEmail;

    @NotBlank(message = "NOT_BLANK")
    @Email(message = "EMAIL_INVALID")
    private String oldEmail;
}
