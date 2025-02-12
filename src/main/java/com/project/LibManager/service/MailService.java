package com.project.LibManager.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.project.LibManager.exception.AppException;
import com.project.LibManager.exception.ErrorCode;

import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MailService {
     JavaMailSender javaMailSender;
     TemplateEngine templateEngine;

    public void sendEmailVerify(String fullName, String token, String email) {
        try {
           MimeMessage message = javaMailSender.createMimeMessage();
           MimeMessageHelper helper = new MimeMessageHelper(message, true);
           
           // Process the template with the given context
           Context context = new Context();
            context.setVariable("name", fullName);
            context.setVariable("verifyUrl", "http://localhost:8080/auth/verify-email?token=" + token);
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
