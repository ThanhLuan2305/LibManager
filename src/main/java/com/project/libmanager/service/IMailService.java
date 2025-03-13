package com.project.libmanager.service;

public interface IMailService {

    void sendEmailVerify(String fullName, String token, String email);

    void sendEmailOTP(String otp, String email, boolean isChangePassword, String name);

    void sendSimpleEmail(String to, String subject, String body);
}
