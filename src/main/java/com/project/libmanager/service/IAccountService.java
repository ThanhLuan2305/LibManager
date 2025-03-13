package com.project.libmanager.service;

import com.project.libmanager.service.dto.request.ChangeMailRequest;
import com.project.libmanager.service.dto.request.RegisterRequest;
import com.project.libmanager.service.dto.request.VerifyChangeMailRequest;
import com.project.libmanager.service.dto.response.UserResponse;

public interface IAccountService {
    UserResponse registerUser(RegisterRequest registerRequest);
    boolean verifyEmail(String otp, String email);
    boolean verifyPhone(String otp, String phone);
    void verifyChangeEmail(VerifyChangeMailRequest request);
    void changeEmail(ChangeMailRequest request);
}
