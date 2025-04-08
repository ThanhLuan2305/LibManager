package com.project.libmanager.service;

import com.project.libmanager.entity.User;
import com.project.libmanager.service.dto.request.LoginDetailRequest;

import java.time.Instant;

public interface ILoginDetailService {
    void createLoginDetail(LoginDetailRequest loginRequest);

    void disableLoginDetailById(String jti);

    void updateLoginDetailIsEnable(String jti, Instant expTime);

    void deleteLoginDetailByUser(User user);
}
