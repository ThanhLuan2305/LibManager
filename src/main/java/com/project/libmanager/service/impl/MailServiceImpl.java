package com.project.libmanager.service.impl;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.service.IMailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Implementation of {@link IMailService} for sending various types of emails.
 * Supports HTML emails via Thymeleaf templates and simple plain-text emails.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MailServiceImpl implements IMailService {
    private final JavaMailSender javaMailSender;     // Handles email sending functionality
    private final TemplateEngine templateEngine;     // Processes Thymeleaf templates for HTML emails

    @Value("${app.verify-email-url}")
    private String verifyEmailUrl;                   // Base URL for email verification links; from config

    @Value("${app.reset-password-url}")
    private String resetPasswordUrl;                 // Base URL for password reset links; from config

    @Value("${app.support-email}")
    private String supportEmail;                     // Sender email address; from config

    private static final String ERROR_EMAIL = "Failed to send email";

    /**
     * Sends a verification email with a token to verify the user's email address.
     *
     * @param fullName the full name of the user (for personalization)
     * @param token    the verification token to include in the link
     * @param email    the recipient's email address
     * @throws AppException if email sending fails (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Uses a Thymeleaf template to generate HTML content with a verification link.
     * Assumes template 'emailTemplate' is configured with 'name' and 'verifyUrl' variables.
     */
    @Override
    public void sendEmailVerify(String fullName, String token, String email) {
        try {
            // Create MIME message for HTML content; supports rich formatting
            MimeMessage message = javaMailSender.createMimeMessage();
            // Helper to simplify MIME message setup; 'true' enables HTML
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Set up Thymeleaf context with variables for template
            Context context = new Context();
            context.setVariable("name", fullName); // Personalize email with user's name
            // Construct verification URL with token and email as query params
            context.setVariable("verifyUrl", verifyEmailUrl + "?otp=" + token + "&email=" + email);
            // Render HTML from 'emailTemplate'; assumes template exists
            String html = templateEngine.process("emailTemplate", context);

            // Configure email properties
            helper.setTo(email);                         // Recipient address
            helper.setSubject("Xác thực Email");         // Subject in Vietnamese
            helper.setText(html, true);             // Set HTML content; 'true' indicates HTML
            helper.setFrom(supportEmail);                // Sender address from config

            // Send email; assumes JavaMailSender is properly configured
            javaMailSender.send(message);

        } catch (Exception e) {
            log.error(ERROR_EMAIL, e);
            // Wrap exception in custom AppException for consistency
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Sends an OTP email for verifying email or password changes.
     *
     * @param otp              the one-time password code to send
     * @param email            the recipient's email address
     * @param isChangePassword true if OTP is for password change, false for email change
     * @param name             the user's name (for personalization)
     * @throws AppException if email sending fails (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Uses a Thymeleaf template 'emailOtpTemplate' with dynamic subject and content
     * based on the purpose (password or email change).
     */
    @Override
    public void sendEmailOTP(String otp, String email, boolean isChangePassword, String name) {
        try {
            // Create MIME message for HTML content
            MimeMessage message = javaMailSender.createMimeMessage();
            // Helper for MIME setup; 'true' enables HTML
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Customize content based on purpose; improves user clarity
            String caption = isChangePassword ? "Xác thực thay đổi mật khẩu" : "Xác thực thay đổi email";
            String subject = isChangePassword ? "Verification Change Password" : "Verification Change Email";
            // Set up Thymeleaf context with variables
            Context context = new Context();
            context.setVariable("caption", caption);     // Dynamic header for email body
            context.setVariable("name", name);           // Personalize with user's name
            context.setVariable("request", caption);     // Reused for consistency in template
            context.setVariable("otpCode", otp);         // OTP code for verification
            // Render HTML from 'emailOtpTemplate'; assumes template exists
            String html = templateEngine.process("emailOtpTemplate", context);

            // Configure email properties
            helper.setTo(email);                         // Recipient address
            helper.setSubject(subject);                  // Dynamic subject based on purpose
            helper.setText(html, true);                  // Set HTML content
            helper.setFrom(supportEmail);                // Sender address from config

            // Send email; relies on JavaMailSender configuration
            javaMailSender.send(message);

        } catch (Exception e) {
            // Log error with details for troubleshooting
            log.error(ERROR_EMAIL, e);
            // Wrap in custom exception for uniform error handling
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Sends a password reset email with a token to reset the user's password.
     *
     * @param fullName the full name of the user (for personalization)
     * @param email    the recipient's email address
     * @param token    the reset token to include in the link
     * @throws AppException if email sending fails (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Uses a Thymeleaf template 'emailResetPassword' with a reset link.
     * Assumes template is configured with 'name' and 'resetUrl' variables.
     */
    @Override
    public void sendEmailResetPassword(String fullName, String email, String token) {
        try {
            // Create MIME message for HTML content
            MimeMessage message = javaMailSender.createMimeMessage();
            // Helper for MIME setup; 'true' enables HTML
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Set up Thymeleaf context with variables
            Context context = new Context();
            context.setVariable("name", fullName);       // Personalize with user's name
            // Construct reset URL with token as query param
            context.setVariable("resetUrl", resetPasswordUrl + "?token=" + token);
            // Render HTML from 'emailResetPassword'; assumes template exists
            String html = templateEngine.process("emailResetPassword", context);

            // Configure email properties
            helper.setTo(email);                         // Recipient address
            helper.setSubject("Reset Password");         // Fixed subject
            helper.setText(html, true);             // Set HTML content
            helper.setFrom(supportEmail);                // Sender address from config

            // Send email; assumes reliable mail server setup
            javaMailSender.send(message);

        } catch (Exception e) {
            // Log failure for debugging
            log.error(ERROR_EMAIL, e);
            // Wrap exception in custom error for consistency
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Sends a simple plain-text email.
     *
     * @param to      the recipient's email address
     * @param subject the subject of the email
     * @param body    the plain-text body content of the email
     * @implNote Uses {@link SimpleMailMessage} for lightweight, text-only emails without
     * HTML formatting. Does not handle exceptions explicitly, relying on
     * JavaMailSender's default behavior.
     */
    @Override
    public void sendSimpleEmail(String to, String subject, String body) {
        // Create simple message for plain-text email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);                               // Recipient address
        message.setSubject(subject);                     // Email subject
        message.setText(body);                           // Plain-text content
        message.setFrom(supportEmail);                   // Sender address from config

        // Send email; assumes no HTML or attachments needed
        javaMailSender.send(message);
    }
}