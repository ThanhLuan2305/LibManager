package com.project.LibManager.service;

import org.springframework.beans.factory.annotation.Autowired;
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

    public void sendEmail(String fullName, String token, String email) {
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

           //send the email
           javaMailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send email", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}
