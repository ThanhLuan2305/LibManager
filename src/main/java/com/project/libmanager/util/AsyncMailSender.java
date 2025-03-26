package com.project.libmanager.util;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.service.IMailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.util.List;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncMailSender {
    private final IMailService emailService;

    @Async
    public void sendMaintenanceEmails(List<String> emails, boolean maintenanceMode) {
        String subject = maintenanceMode ? "🔧 Hệ thống đang bảo trì" : "✅ Hệ thống đã hoạt động trở lại";
        String body = maintenanceMode
                ? "Xin chào, hệ thống thư viện đang trong quá trình bảo trì. Vui lòng quay lại sau!"
                : "Hệ thống đã hoạt động bình thường. Cảm ơn bạn đã chờ đợi!";

        for (String email : emails) {
            try {
                emailService.sendSimpleEmail(email, subject, body);
            } catch (Exception e) {
                log.error("Lỗi gửi email tới: " + email + " - " + e.getMessage());
                throw  new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        }
    }
}
