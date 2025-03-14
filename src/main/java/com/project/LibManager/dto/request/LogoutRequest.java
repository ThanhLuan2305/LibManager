package com.project.LibManager.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LogoutRequest {
    private String accessToken;
    private String refreshToken;
}
