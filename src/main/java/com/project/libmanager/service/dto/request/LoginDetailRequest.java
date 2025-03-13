package com.project.libmanager.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
public class LoginDetailRequest {

    private String jti;

    private boolean enabled;

    private Instant expiredAt;

    private String email;
}
