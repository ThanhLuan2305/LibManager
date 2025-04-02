package com.project.libmanager.util;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.service.IMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Utility class for asynchronously sending maintenance notification emails.
 * Handles sending emails to a list of recipients based on system maintenance status.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncMailSender {
    private final IMailService emailService; // Service for sending emails

    /**
     * Asynchronously sends maintenance-related emails to a list of recipients.
     *
     * @param emails          the {@link List} of email addresses to receive notifications
     * @param maintenanceMode true if system is entering maintenance, false if resuming operation
     * @throws AppException if email sending fails (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Uses Spring's @Async to send emails in a separate thread, with dynamic subject and body
     * based on maintenance mode. Logs errors and rethrows as a custom exception.
     */
    @Async
    public void sendMaintenanceEmails(List<String> emails, boolean maintenanceMode) {
        // Determine email content; conditional based on maintenance status
        String subject = maintenanceMode ? "🔧 Hệ thống đang bảo trì" : "✅ Hệ thống đã hoạt động trở lại";
        String body = maintenanceMode
                ? "Xin chào, hệ thống thư viện đang trong quá trình bảo trì. Vui lòng quay lại sau!"
                : "Hệ thống đã hoạt động bình thường. Cảm ơn bạn đã chờ đợi!";

        // Iterate over recipients; sends email to each
        for (String email : emails) {
            try {
                // Send email; delegates to IMailService, assumes simple text email
                emailService.sendSimpleEmail(email, subject, body);
            } catch (Exception e) {
                log.error("Lỗi gửi email tới: " + email + " - " + e.getMessage());
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        }
    }
}