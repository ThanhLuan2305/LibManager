package com.project.libmanager.service.dto.response;

import java.time.Instant;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;

    private String email;

    private String phoneNumber;

    private String fullName;

    private Instant birthDate;

    private boolean verified;

    private boolean deleted;

    private boolean resetPassword;

    private int lateReturnCount;

    private Set<RoleResponse> roles;

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;
}
