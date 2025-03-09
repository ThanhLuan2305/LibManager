package com.project.libmanager.service;

import com.nimbusds.jose.JOSEException;
import com.project.libmanager.service.dto.request.ChangePasswordRequest;
import com.project.libmanager.service.dto.response.ChangePassAfterResetRequest;

import java.text.ParseException;

public interface IPasswordService {
    boolean changePassword(ChangePasswordRequest request);
    boolean changePasswordAfterReset(ChangePassAfterResetRequest request);
    void forgetPassword(String email);
    String verifyOTP(Integer token, String email);
    String resetPassword(String token) throws JOSEException, ParseException;
}
