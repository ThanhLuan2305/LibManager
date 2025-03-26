package com.project.libmanager.service.impl;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.constant.UserAction;
import com.project.libmanager.entity.ActivityLog;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.repository.ActivityLogRepository;
import com.project.libmanager.service.IActivityLogService;
import com.project.libmanager.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogServiceServiceImpl implements IActivityLogService {
    private final ActivityLogRepository activityLogRepository;
    @Override
    public void logAction(Long userId, String email, UserAction action, String details) {
        ActivityLog logActivity = ActivityLog.builder()
                .userId(userId)
                .email(email)
                .action(action)
                .details(details)
                .timestamp(Instant.now())
                .build();
        ActivityLog newLog = activityLogRepository.save(logActivity);
        log.info("Check activity log: {}", newLog);
    }

    @Override
    public Page<ActivityLog> getActivityLog(Pageable pageable) {
        return activityLogRepository.findAll(pageable);
    }

    @Override
    public void deleteAllLogs() {
        activityLogRepository.deleteAll();
    }

}
