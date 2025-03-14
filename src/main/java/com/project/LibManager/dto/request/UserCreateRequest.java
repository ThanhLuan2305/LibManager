package com.project.LibManager.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/d")
    @DateTimeFormat(pattern = "yyyy/MM/d")
    private LocalDate birthDate;

    @NotNull(message = "NOT_BLANK")
    private Boolean isVerified;

    @NotNull(message = "NOT_BLANK")
    private List<String> listRole;

    @NotNull(message = "NOT_BLANK")
    private Boolean isDeleted;

    @NotNull(message = "NOT_BLANK")
    private Boolean isReset;
}

