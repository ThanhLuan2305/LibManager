package com.project.libmanager.service;

import com.project.libmanager.service.dto.request.ChangePasswordRequest;
import com.project.libmanager.service.dto.request.ResetPasswordRequest;


public interface IPasswordService {
    boolean changePassword(ChangePasswordRequest request);

    void forgetPassword(String email);

    void resetPassword(ResetPasswordRequest resetPasswordRequest);
}
