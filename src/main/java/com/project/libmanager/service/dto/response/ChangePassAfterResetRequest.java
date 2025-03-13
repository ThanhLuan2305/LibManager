package com.project.libmanager.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePassAfterResetRequest {
    private String email;
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
