package com.project.LibManager.dto.request;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserUpdateRequest {
    @Size(min = 6, message = "INVALID_PASSWORD")
    private String password;

    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    private String fullName;

    @Past(message = "BIRTH_DATE_MUST_BE_IN_PAST")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/d")
    @DateTimeFormat(pattern = "yyyy/MM/d")
    private LocalDate birthDate;

    private Boolean isVerified;

    private List<String> listRole;

    private Boolean isDeleted;

    private Boolean isReset;

    private int lateReturnCount;
}
