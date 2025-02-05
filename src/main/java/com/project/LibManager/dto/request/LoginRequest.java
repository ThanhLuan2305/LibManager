package com.project.LibManager.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
	@NotBlank(message = "NOT_BLANK")
    @Email(message = "EMAIL_INVALID")
    private String email;
    @NotBlank(message = "NOT_BLANK")
    @Size(min = 6, message = "INVALID_PASSWORD")
    private String password;
}
