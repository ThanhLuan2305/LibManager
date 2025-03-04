package com.project.LibManager.service;

public interface IMailService {

    void sendEmailVerify(String fullName, String token, String email);

    void sendEmailOTP(Integer otp, String email, boolean isChangePassword, String name);

    void sendSimpleEmail(String to, String subject, String body);
}
