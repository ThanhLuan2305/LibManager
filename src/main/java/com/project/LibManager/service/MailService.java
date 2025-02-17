package com.project.LibManager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.project.LibManager.constant.ErrorCode;
import com.project.LibManager.exception.AppException;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.verify-email-url}")
    private String VERIFY_EMAIL_URL;


    public void sendEmailVerify(String fullName, String token, String email) {
        try {
           MimeMessage message = javaMailSender.createMimeMessage();
           MimeMessageHelper helper = new MimeMessageHelper(message, true);
           
           // Process the template with the given context
           Context context = new Context();
            context.setVariable("name", fullName);
            context.setVariable("verifyUrl", VERIFY_EMAIL_URL + token);
           String html = templateEngine.process("emailTemplate", context);

           // Set email properties
           helper.setTo(email);
           helper.setSubject("Xác thực Email");
           helper.setText(html, true);
           helper.setFrom("libmanage.support@gmail.com");

           //send the email
           javaMailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send email", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
    public void sendEmailOTP( Integer otp, String email, boolean isChangePassword, String name) {
        try {
           MimeMessage message = javaMailSender.createMimeMessage();
           MimeMessageHelper helper = new MimeMessageHelper(message, true);
           
           String caption = isChangePassword ? "Xác thực thay đổi mật khẩu" : "Xác thực thay đổi email";
           // Process the template with the given context
           Context context = new Context();
            context.setVariable("caption", caption);
            context.setVariable("name", name);
            context.setVariable("request", caption);
            context.setVariable("otpCode", otp);
           String html = templateEngine.process("emailOtpTemplate", context);

           // Set email properties
           helper.setTo(email);
           helper.setSubject("Xác thực Email");
           helper.setText(html, true);
           helper.setFrom("linmanage.support@gmail.com");

           //send the email
           javaMailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send email", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public void sendSimpleEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("libmanage.support@gmail.com");
        
        javaMailSender.send(message);
    }
}
