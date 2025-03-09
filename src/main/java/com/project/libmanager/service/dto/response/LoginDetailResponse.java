package com.project.libmanager.service.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginDetailResponse {
    private Long id;

    private String jti;

    private boolean enabled;

    private Instant expiredAt;

    private UserResponse user;
}
