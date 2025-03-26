package com.project.libmanager.service.impl;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.constant.UserAction;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.repository.UserRepository;
import com.project.libmanager.service.IActivityLogService;
import com.project.libmanager.util.AsyncMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.project.libmanager.service.IMaintenanceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements IMaintenanceService {
    private final UserRepository userRepository;
    private final AsyncMailSender asyncMailSender;
    private final IActivityLogService activityLogService;
    @Value("${app.maintenance-mode:false}")
    private boolean maintenanceMode;

    /**
     * Returns the current status of the maintenance mode.
     * 
     * @return {@code true} if the application is in maintenance mode, {@code false}
     *         otherwise.
     */
    @Override
    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    /**
     * Sets the maintenance mode status.
     * 
     * @param maintenanceMode The desired maintenance mode status.
     *                        Pass {@code true} to enable maintenance mode,
     *                        {@code false} to disable it.
     */
    @Override
    public void setMaintenanceMode(boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;

        List<String> emails = userRepository.findAll().stream()
                .map(User::getEmail)
                .filter(email -> email != null && !email.isEmpty())
                .toList();

        User user = getAuthenticatedUser();
        activityLogService.logAction(
                user.getId(),
                user.getEmail(),
                UserAction.SYSTEM_MAINTENANCE_MODE,
                "Admin set maintenance mode is: " + maintenanceMode
        );
        asyncMailSender.sendMaintenanceEmails(emails, maintenanceMode);
    }

    private User getAuthenticatedUser() {
        SecurityContext jwtContext = SecurityContextHolder.getContext();
        if (jwtContext == null || jwtContext.getAuthentication() == null ||
                !jwtContext.getAuthentication().isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        log.info("Authentication {}", jwtContext.getAuthentication().getName());

        String email = jwtContext.getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}
