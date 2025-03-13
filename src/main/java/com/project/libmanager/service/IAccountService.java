package com.project.libmanager.service;

import com.project.libmanager.service.dto.request.*;
import com.project.libmanager.service.dto.response.UserResponse;

public interface IAccountService {
    UserResponse registerUser(RegisterRequest registerRequest);
    boolean verifyEmail(String otp, String email);
    boolean verifyPhone(String otp, String phone);
    void verifyChangeEmail(VerifyChangeMailRequest request);
    void changeEmail(ChangeMailRequest request);
    void verifyChangePhone(VerifyChangePhoneRequest request);
    void changePhone(ChangePhoneRequest request);
}
