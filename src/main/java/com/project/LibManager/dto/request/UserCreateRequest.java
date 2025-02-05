package com.project.LibManager.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class UserCreateRequest {
    @NotBlank(message = "NOT_BLANK")
    @Email(message = "EMAIL_INVALID")
    private String email;
    @NotBlank(message = "NOT_BLANK")
    @Size(min = 6, message = "INVALID_PASSWORD")
    private String password;
    @NotBlank(message = "NOT_BLANK")
    private String fullName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate birthDate;
}
