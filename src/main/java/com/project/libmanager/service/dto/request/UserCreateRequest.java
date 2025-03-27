package com.project.libmanager.service.dto.request;

import com.project.libmanager.constant.VerificationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {
    @NotBlank(message = "NOT_BLANK")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "EMAIL_INVALID")
    private String email;

    @NotBlank(message = "NOT_BLANK")
    @Size(min = 10, max = 10, message = "PHONE_INVALID")
    private String phoneNumber;

    @NotBlank(message = "NOT_BLANK")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$", message = "INVALID_PASSWORD")
    private String password;

    @NotBlank(message = "NOT_BLANK")
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    private String fullName;

    @NotNull(message = "NOT_BLANK")
    @Past(message = "BIRTH_DATE_MUST_BE_IN_PAST")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant birthDate;

    @NotNull(message = "NOT_BLANK")
    private VerificationStatus verificationStatus;

    @NotNull(message = "NOT_BLANK")
    private List<String> listRole;

    @NotNull(message = "NOT_BLANK")
    private boolean deleted;

    @NotNull(message = "NOT_BLANK")
    private boolean resetPassword;
}
