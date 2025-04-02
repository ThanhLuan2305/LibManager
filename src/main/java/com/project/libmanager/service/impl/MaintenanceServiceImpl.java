package com.project.libmanager.service.impl;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.constant.UserAction;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.repository.UserRepository;
import com.project.libmanager.service.IActivityLogService;
import com.project.libmanager.service.IMaintenanceService;
import com.project.libmanager.util.AsyncMailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of {@link IMaintenanceService} for managing application maintenance mode.
 * Provides methods to check and toggle maintenance mode, notifying users via email.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements IMaintenanceService {
    private final UserRepository userRepository;        // Handles user data retrieval
    private final AsyncMailSender asyncMailSender;      // Sends maintenance emails asynchronously
    private final IActivityLogService activityLogService; // Logs admin actions for audit trail
    @Value("${app.maintenance-mode:false}")
    private boolean maintenanceMode;

    /**
     * Returns the current status of the maintenance mode.
     *
     * @return {@code true} if maintenance mode is enabled, {@code false} otherwise
     * @implNote Simply returns the current value of the maintenanceMode field, which is
     * configurable via application properties.
     */
    @Override
    public boolean isMaintenanceMode() {
        // Return current state; reflects runtime configuration or manual toggle
        return maintenanceMode;
    }

    /**
     * Sets the maintenance mode status and notifies all users.
     *
     * @param maintenanceMode the desired status: {@code true} to enable maintenance mode,
     *                        {@code false} to disable it
     * @throws AppException if:
     *                      - admin is not authenticated (ErrorCode.UNAUTHORIZED)
     *                      - admin user not found (ErrorCode.USER_NOT_EXISTED)
     * @implNote Updates the maintenance mode flag, logs the action, and sends emails to all
     * users asynchronously. Only authenticated admins can perform this action.
     */
    @Override
    public void setMaintenanceMode(boolean maintenanceMode) {
        // Update instance field; overrides config value until restart
        this.maintenanceMode = maintenanceMode;

        // Fetch all user emails; filters out null/empty values for robustness
        List<String> emails = userRepository.findAll().stream()
                .map(User::getEmail)
                .filter(email -> email != null && !email.isEmpty())
                .toList();

        // Fetch authenticated admin; ensures only authorized users can toggle mode
        User user = getAuthenticatedUser();
        // Log action for audit; no state change tracked as it's a system-level action
        activityLogService.logAction(
                user.getId(),
                user.getEmail(),
                UserAction.SYSTEM_MAINTENANCE_MODE,
                "Admin set maintenance mode is: " + maintenanceMode,
                null,
                null
        );
        // Send emails asynchronously; assumes AsyncMailSender handles failures gracefully
        asyncMailSender.sendMaintenanceEmails(emails, maintenanceMode);
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     *
     * @return a {@link User} entity representing the authenticated admin
     * @throws AppException if:
     *                      - security context is invalid or missing (ErrorCode.UNAUTHORIZED)
     *                      - user not found by email (ErrorCode.USER_NOT_EXISTED)
     * @implNote Extracts email from JWT principal and fetches the corresponding user.
     */
    private User getAuthenticatedUser() {
        // Access security context; assumes JWT-based authentication
        SecurityContext jwtContext = SecurityContextHolder.getContext();
        // Validate context and authentication; fails fast if invalid
        if (jwtContext == null || jwtContext.getAuthentication() == null ||
                !jwtContext.getAuthentication().isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED); // No authenticated user
        }
        // Log principal for debugging; assumes email is the JWT subject
        log.info("Authentication {}", jwtContext.getAuthentication().getName());

        // Extract email from authentication; assumes unique in system
        String email = jwtContext.getAuthentication().getName();
        // Fetch user; assumes email is a reliable identifier
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)); // Fail if user missing
    }
}