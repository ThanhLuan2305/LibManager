package com.project.libmanager.service;

import com.project.libmanager.constant.OtpType;
import com.project.libmanager.entity.OtpVerification;

public interface IOtpVerificationService {
    void createOtp(OtpVerification otpVerification, boolean isPhone);
    void deleteOtp(String contactInfo, OtpType type, boolean isPhone);
    boolean verifyOtp(String otp, String contactInfo, OtpType type, boolean isPhone);
}
