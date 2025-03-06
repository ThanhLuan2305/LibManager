package com.project.libmanager.service.dto.request;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "NOT_BLANK")
    @Email(message = "EMAIL_INVALID")
    private String email;

    @NotBlank(message = "NOT_BLANK")
    @Size(min = 6, message = "INVALID_PASSWORD")
    private String password;

    @NotBlank(message = "NOT_BLANK")
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    private String fullName;

    @NotNull(message = "NOT_BLANK")
    @Past(message = "BIRTH_DATE_MUST_BE_IN_PAST")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant birthDate;
}
