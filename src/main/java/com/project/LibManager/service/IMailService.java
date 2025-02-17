package com.project.LibManager.service;

public interface IMailService {

    public void sendEmailVerify(String fullName, String token, String email);

    public void sendEmailOTP( Integer otp, String email, boolean isChangePassword, String name);

    public void sendSimpleEmail(String to, String subject, String body);
}
