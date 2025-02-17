package com.project.LibManager.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.project.LibManager.constant.ErrorCode;
import com.project.LibManager.exception.AppException;
import com.project.LibManager.service.IMailService;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailServiceImpl implements IMailService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.verify-email-url}")
    private String VERIFY_EMAIL_URL;

    @Value("${app.support-email}")
    private String SUPPORT_EMAIL;

    @Override
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
           helper.setFrom(SUPPORT_EMAIL);

           //send the email
           javaMailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send email", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
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
           helper.setFrom(SUPPORT_EMAIL);

           //send the email
           javaMailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send email", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public void sendSimpleEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(SUPPORT_EMAIL);
        
        javaMailSender.send(message);
    }
}
