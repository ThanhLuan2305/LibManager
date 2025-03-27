package com.project.libmanager.service;

import com.project.libmanager.service.dto.request.ChangeMailRequest;
import com.project.libmanager.service.dto.request.ChangePhoneRequest;
import com.project.libmanager.service.dto.request.RegisterRequest;
import com.project.libmanager.service.dto.request.VerifyChangeMailRequest;
import com.project.libmanager.service.dto.request.VerifyChangePhoneRequest;
import com.project.libmanager.service.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface IAccountService {
    UserResponse registerUser(RegisterRequest registerRequest);
    boolean verifyEmail(String otp, String email);
    boolean verifyPhone(String otp, String phone);
    void verifyChangeEmail(VerifyChangeMailRequest request);
    void changeEmail(ChangeMailRequest request);
    void verifyChangePhone(VerifyChangePhoneRequest request);
    void changePhone(ChangePhoneRequest request);
    List<String> getRolesUser(String token, HttpServletResponse response);
}
