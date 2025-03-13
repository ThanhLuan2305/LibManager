package com.project.libmanager.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.service.IMailService;

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
    private String verifyEmailUrl;

    @Value("${app.support-email}")
    private String supportEmail;

    /**
     * Sends a verification email to the user with a provided token for email
     * verification.
     * 
     * @param fullName The full name of the user.
     * @param token    The token for email verification.
     * @param email    The recipient's email address.
     * @throws AppException If the email cannot be sent or any other error occurs.
     * @implNote This method uses a Thymeleaf template to generate the HTML content
     *           for the email.
     *           The email includes a link with the verification token to allow the
     *           user to verify their email.
     */
    @Override
    public void sendEmailVerify(String fullName, String token, String email) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Process the template with the given context
            Context context = new Context();
            context.setVariable("name", fullName);
            context.setVariable("verifyUrl", verifyEmailUrl + token + "&email=" + email);
            String html = templateEngine.process("emailTemplate", context);

            // Set email properties
            helper.setTo(email);
            helper.setSubject("Xác thực Email");
            helper.setText(html, true);
            helper.setFrom(supportEmail);

            // send the email
            javaMailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send email", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Sends an OTP (One-Time Password) email for verifying email changes or
     * password changes.
     * 
     * @param otp              The OTP code to be sent.
     * @param email            The recipient's email address.
     * @param isChangePassword Whether the OTP is for changing the password or the
     *                         email.
     * @param name             The name of the user.
     * @throws AppException If the email cannot be sent or any other error occurs.
     * @implNote This method uses a Thymeleaf template to generate the HTML content
     *           for the OTP email.
     *           It also customizes the subject and content based on whether it's a
     *           password or email change.
     */
    @Override
    public void sendEmailOTP(String otp, String email, boolean isChangePassword, String name) {
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
            helper.setFrom(supportEmail);

            // send the email
            javaMailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send email", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Sends a simple plain-text email to the recipient.
     * 
     * @param to      The recipient's email address.
     * @param subject The subject of the email.
     * @param body    The body content of the email.
     * @implNote This method sends a simple email without any HTML formatting, using
     *           the JavaMailSender API.
     */
    @Override
    public void sendSimpleEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(supportEmail);

        javaMailSender.send(message);
    }
}
