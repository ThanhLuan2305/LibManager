package com.project.libmanager.service;

import com.project.libmanager.service.dto.request.ChangePasswordRequest;
import com.project.libmanager.service.dto.response.ChangePassAfterResetRequest;


public interface IPasswordService {
    boolean changePassword(ChangePasswordRequest request);
    boolean changePasswordAfterReset(ChangePassAfterResetRequest request);
    void forgetPassword(String contactInfo, boolean isPhone);
    String resetPassword(String token, String contactInfo, boolean isPhone);
}
