package com.project.libmanager.service.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.libmanager.constant.VerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a new user")
public class UserCreateRequest {
    @NotBlank(message = "NOT_BLANK")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "EMAIL_INVALID")
    @Schema(description = "User's email address", example = "user@example.com")
    private String email;

    @NotBlank(message = "NOT_BLANK")
    @Size(min = 10, max = 10, message = "PHONE_INVALID")
    @Schema(description = "User's phone number, must be exactly 10 digits", example = "0123456789")
    private String phoneNumber;

    @NotBlank(message = "NOT_BLANK")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$", message = "INVALID_PASSWORD")
    @Schema(description = "User's password, must be at least 8 characters with letters, numbers, and special characters", example = "Password123!")
    private String password;

    @NotBlank(message = "NOT_BLANK")
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    @Schema(description = "User's full name", example = "John Doe")
    private String fullName;

    @NotNull(message = "NOT_BLANK")
    @Past(message = "BIRTH_DATE_MUST_BE_IN_PAST")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'HH:mm:ss.SSSX", timezone = "UTC")
    @Schema(description = "User's birth date, must be in the past", example = "01-01-1990T00:00:00.000Z")
    private Instant birthDate;

    @NotNull(message = "NOT_BLANK")
    @Schema(description = "Verification status of the user", example = "VERIFIED")
    private VerificationStatus verificationStatus;

    @NotNull(message = "NOT_BLANK")
    @Schema(description = "List of roles assigned to the user", example = "[\"USER\"]")
    private List<String> listRole;

    @NotNull(message = "NOT_BLANK")
    @Schema(description = "Whether the user is marked as deleted", example = "false")
    private boolean deleted;
}