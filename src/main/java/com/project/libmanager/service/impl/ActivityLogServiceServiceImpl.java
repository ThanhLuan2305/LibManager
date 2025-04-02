package com.project.libmanager.service.impl;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.constant.UserAction;
import com.project.libmanager.entity.ActivityLog;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.repository.ActivityLogRepository;
import com.project.libmanager.service.IActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Implementation of {@link IActivityLogService} for managing activity logging operations.
 * Provides methods to log user actions, retrieve logs, and delete logs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogServiceServiceImpl implements IActivityLogService {
    private final ActivityLogRepository activityLogRepository; // Repository for persisting and querying activity logs

    /**
     * Logs a user action with relevant details and optional before/after states.
     *
     * @param userId       the ID of the user performing the action
     * @param email        the email of the user (for identification and auditing)
     * @param action       the {@link UserAction} enum representing the type of action
     * @param details      a string describing the action (e.g., "User logged in")
     * @param beforeChange the state before the action (can be null)
     * @param afterChange  the state after the action (can be null)
     * @implNote Creates and saves an {@link ActivityLog} entity, then logs the saved entity for verification.
     */
    @Override
    public void logAction(Long userId, String email, UserAction action, String details, Object beforeChange, Object afterChange) {
        // Build log entity; captures action details with current timestamp
        ActivityLog logActivity = ActivityLog.builder()
                .userId(userId)          // Links log to user
                .email(email)            // Provides user identifier
                .action(action)          // Specifies action type
                .details(details)        // Describes action context
                .timestamp(Instant.now()) // Records exact time of action
                .beforeChange(beforeChange) // Optional: state before action
                .afterChange(afterChange)   // Optional: state after action
                .build();

        // Persist log; assumes auto-generated ID
        ActivityLog newLog = activityLogRepository.save(logActivity);
        // Log saved entity; aids debugging and verification
        log.info("Check activity log: {}", newLog);
    }

    /**
     * Retrieves a paginated list of all activity logs.
     *
     * @param pageable the {@link Pageable} object with pagination details (e.g., page number, size)
     * @return a {@link Page} of {@link ActivityLog} entities
     * @implNote Fetches logs from the repository with pagination support.
     */
    @Override
    public Page<ActivityLog> getActivityLogs(Pageable pageable) {
        // Fetch all logs with pagination; assumes repository handles sorting/filtering if specified
        return activityLogRepository.findAll(pageable);
    }

    /**
     * Deletes all activity logs from the system.
     *
     * @implNote Performs a bulk delete operation on the log repository.
     */
    @Override
    public void deleteAllLogs() {
        // Delete all logs; assumes no additional validation required
        activityLogRepository.deleteAll();
    }

    /**
     * Retrieves a specific activity log by its ID.
     *
     * @param id the ID of the activity log to retrieve
     * @return the {@link ActivityLog} entity corresponding to the ID
     * @throws AppException if the log is not found (ErrorCode.ACTIVITY_LOG_NOT_EXISTED)
     * @implNote Fetches a single log from the repository, throwing an exception if not found.
     */
    @Override
    public ActivityLog getActivityLog(String id) {
        // Fetch log by ID; uses Optional to handle absence, throws if not found
        return activityLogRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACTIVITY_LOG_NOT_EXISTED));
    }
}