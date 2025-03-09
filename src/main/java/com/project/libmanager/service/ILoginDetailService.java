package com.project.libmanager.service;

import com.project.libmanager.service.dto.request.LoginDetailRequest;
import com.project.libmanager.service.dto.response.LoginDetailResponse;

import java.time.Instant;

public interface ILoginDetailService {
    void createLoginDetail(LoginDetailRequest loginRequest);
    void updateLoginDetail(LoginDetailRequest loginRequest, Long id);
    void disableLoginDetailById(String jti);
    void updateLoginDetailIsEnable(String jti, Instant expTime);
    LoginDetailResponse getLoginDetailByJti(String jti);
}
