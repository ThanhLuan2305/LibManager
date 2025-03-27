package com.project.libmanager.service;

import com.project.libmanager.constant.UserAction;
import com.project.libmanager.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IActivityLogService {
    void logAction(Long userId, String email, UserAction action, String details, Object beforeChange, Object afterChange);
    Page<ActivityLog> getActivityLogs(Pageable pageable);
    void deleteAllLogs();
    ActivityLog getActivityLog(String id);
}
