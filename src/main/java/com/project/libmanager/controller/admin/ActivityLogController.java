package com.project.libmanager.controller.admin;

import com.project.libmanager.entity.ActivityLog;
import com.project.libmanager.service.IActivityLogService;
import com.project.libmanager.service.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/activity-log")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
public class ActivityLogController {
    private final IActivityLogService activityLogService;
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ActivityLog>>> getActivityLogs(@RequestParam(defaultValue = "0") int offset,
                                                                   @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(offset, limit, Sort.by("timestamp").descending());
        ApiResponse<Page<ActivityLog>> response = ApiResponse.<Page<ActivityLog>>builder()
                .result(activityLogService.getActivityLogs(pageable))
                .message("Activity retrieved successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> deleteAllActivityLog() {
        activityLogService.deleteAllLogs();
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Delete Activity successfully")
                .result("success")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ActivityLog>> getActivityLog(@PathVariable String id) {
       ApiResponse<ActivityLog> response = ApiResponse.<ActivityLog>builder()
                .result(activityLogService.getActivityLog(id))
                .message("Activity retrieved successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
