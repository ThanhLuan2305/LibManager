package com.project.LibManager.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "INVALID_PASSWORD")
    private String password;
    @NotBlank(message = "Name không được để trống")
    private String fullName;
    @NotBlank(message = "BirthDate không được để trống")
    private LocalDate birthDate;
}
