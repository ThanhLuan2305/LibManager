package com.project.libmanager.controller.admin;

import com.project.libmanager.entity.ActivityLog;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.service.IActivityLogService;
import com.project.libmanager.service.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * REST controller for managing activity logs for admin users.
 * Provides endpoints for retrieving and deleting activity logs.
 */
@RestController
@RequestMapping("admin/activity-log")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
@Tag(name = "Activity Log Management", description = "Endpoints for managing activity logs by admin users")
public class ActivityLogController {
    private final IActivityLogService activityLogService;

    /**
     * Retrieves a paginated list of activity logs, sorted by timestamp in descending order.
     *
     * @param offset the page number (starting from 0)
     * @param limit  thenu mber of items per page
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a {@link Page} of {@link ActivityLog} objects
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - invalid pagination parameters (ErrorCode.INVALID_INPUT)
     * @implNote Uses {@link Pageable} to fetch activity logs from {@link IActivityLogService} and wraps them in an {@link ApiResponse}.
     */
    @GetMapping
    @Operation(summary = "Get activity logs",
            description = "Retrieves a paginated list of activity logs, sorted by timestamp in descending order.")
    @Parameter(name = "offset", description = "Page number (default: 0)")
    @Parameter(name = "limit", description = "Items per page (default: 10)")
    public ResponseEntity<ApiResponse<Page<ActivityLog>>> getActivityLogs(@RequestParam(defaultValue = "0") int offset,
                                                                          @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(offset, limit, Sort.by("timestamp").descending());
        ApiResponse<Page<ActivityLog>> response = ApiResponse.<Page<ActivityLog>>builder()
                .result(activityLogService.getActivityLogs(pageable))
                .message("Activity retrieved successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes all activity logs.
     *
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a success message
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - user not authorized (ErrorCode.UNAUTHORIZED)
     * @implNote Delegates deletion to {@link IActivityLogService} and returns a success message in an {@link ApiResponse}.
     */
    @DeleteMapping
    @Operation(summary = "Delete all activity logs",
            description = "Deletes all activity logs.")
    public ResponseEntity<ApiResponse<String>> deleteAllActivityLog() {
        activityLogService.deleteAllLogs();
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Delete Activity successfully")
                .result("success")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a specific activity log by its ID.
     *
     * @param id the ID of the activity log to retrieve
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with an {@link ActivityLog} object
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - activity log not found (ErrorCode.ACTIVITY_LOG_NOT_FOUND)
     * @implNote Fetches the activity log from {@link IActivityLogService} and returns the details in an {@link ApiResponse}.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get activity log by ID",
            description = "Retrieves a specific activity log by its ID.")
    public ResponseEntity<ApiResponse<ActivityLog>> getActivityLog(@PathVariable String id) {
        ApiResponse<ActivityLog> response = ApiResponse.<ActivityLog>builder()
                .result(activityLogService.getActivityLog(id))
                .message("Activity retrieved successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}