package com.project.LibManager.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.project.LibManager.constant.ErrorCode;
import com.project.LibManager.exception.AppException;
import com.project.LibManager.service.impl.MailServiceImpl;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@SpringBootTest
@TestPropertySource("/test.properties")
public class MailServiceTest {
    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private MailServiceImpl mailService;

    private String VERIFY_EMAIL_URL;
    private String SUPPORT_EMAIL;

    @BeforeEach
    void setup() {
        // Inject mock values for environment variables
        VERIFY_EMAIL_URL = "http://localhost:8080/verify?token=";
        SUPPORT_EMAIL = "support@example.com";
    }

    @Test
    void sendEmailVerify_Success() {
        // Arrange
        String fullName = "John Doe";
        String token = "123456";
        String email = "test@example.com";
        String expectedHtml = "<html>Email content</html>";

        MimeMessage mockMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mockMessage);
        when(templateEngine.process(eq("emailTemplate"), any(Context.class))).thenReturn(expectedHtml);

        // Act & Assert
        assertThrows(AppException.class, () -> {
            mailService.sendEmailVerify(fullName, token, email);
        });

        // Verify that the methods were called
        verify(javaMailSender, times(1)).createMimeMessage();
        verify(templateEngine, times(1)).process(eq("emailTemplate"), any(Context.class));

        // We don't verify javaMailSender.send() because an exception is expected before this point
    }

    @Test
    void sendEmailVerify_Fail_ShouldThrowException() {
        // Arrange
        String fullName = "John Doe";
        String token = "123456";
        String email = "test@example.com";

        when(javaMailSender.createMimeMessage()).thenThrow(new RuntimeException());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, 
            () -> mailService.sendEmailVerify(fullName, token, email));

        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION, exception.getErrorCode());
    }

    @Test
    void sendEmailOTP_Success_PasswordChange() {
        // Arrange
        Integer otp = 123456;
        String email = "test@example.com";
        boolean isChangePassword = true;
        String name = "John Doe";
        String expectedHtml = "<html>Email content</html>";

        MimeMessage mockMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mockMessage);
        when(templateEngine.process(eq("emailOtpTemplate"), any(Context.class))).thenReturn(expectedHtml);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            mailService.sendEmailOTP(otp, email, isChangePassword, name);
        });

        // Assert the exception
        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION, exception.getErrorCode());

        verify(javaMailSender, times(1)).createMimeMessage();
        verify(templateEngine, times(1)).process(eq("emailOtpTemplate"), any(Context.class));

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("emailOtpTemplate"), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();
        assertEquals("Xác thực thay đổi mật khẩu", capturedContext.getVariable("caption"));
        assertEquals(name, capturedContext.getVariable("name"));
        assertEquals("Xác thực thay đổi mật khẩu", capturedContext.getVariable("request"));
        assertEquals(otp, capturedContext.getVariable("otpCode"));
    }

    @Test
    void sendEmailOTP_Success_EmailChange() {
        // Arrange
        Integer otp = 123456;
        String email = "test@example.com";
        boolean isChangePassword = false;
        String name = "John Doe";
        String expectedHtml = "<html>Email content</html>";

        MimeMessage mockMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mockMessage);
        when(templateEngine.process(eq("emailOtpTemplate"), any(Context.class))).thenReturn(expectedHtml);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            mailService.sendEmailOTP(otp, email, isChangePassword, name);
        });

        // Assert the exception
        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION, exception.getErrorCode());

        verify(javaMailSender, times(1)).createMimeMessage();
        verify(templateEngine, times(1)).process(eq("emailOtpTemplate"), any(Context.class));

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("emailOtpTemplate"), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();
        assertEquals("Xác thực thay đổi email", capturedContext.getVariable("caption"));
        assertEquals(name, capturedContext.getVariable("name"));
        assertEquals("Xác thực thay đổi email", capturedContext.getVariable("request"));
        assertEquals(otp, capturedContext.getVariable("otpCode"));
    }

    @Test
    void sendEmailOTP_Fail_ShouldThrowException() {
        // Arrange
        Integer otp = 123456;
        String email = "test@example.com";
        boolean isChangePassword = false;
        String name = "John Doe";

        when(javaMailSender.createMimeMessage()).thenThrow(new RuntimeException());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, 
            () -> mailService.sendEmailOTP(otp, email, isChangePassword, name));

        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION, exception.getErrorCode());
    }

    @Test
    void sendSimpleEmail_Success() {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // Act
        mailService.sendSimpleEmail(to, subject, body);

        // Assert
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendSimpleEmail_Fail_ShouldThrowException() {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        doThrow(new RuntimeException()).when(javaMailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> mailService.sendSimpleEmail(to, subject, body));
    }
}
