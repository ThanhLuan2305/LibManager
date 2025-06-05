package com.project.libmanager.controller.common;

import com.project.libmanager.exception.AppException;
import com.project.libmanager.service.IMaintenanceService;
import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.MaintenanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * REST controller for managing system maintenance mode status.
 * Provides an endpoint to check if the system is in maintenance mode.
 */
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
@Tag(name = "Maintenance Mode Management", description = "Endpoint for checking system maintenance mode status")
public class MaintenanceModeController {
    private final IMaintenanceService maintenanceService;

    /**
     * Retrieves the current maintenance mode status of the system.
     *
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a {@link MaintenanceResponse} detailing the maintenance status
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     * @implNote Uses {@link IMaintenanceService} to check maintenance mode and returns the status in an {@link ApiResponse}.
     */
    @GetMapping("/maintenance/status")
    @Operation(summary = "Get maintenance mode status",
            description = "Retrieves the current maintenance mode status of the system.")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> getMaintenanceMode() {
        boolean isMaintenance = maintenanceService.isMaintenanceMode();

        String message = isMaintenance
                ? "The system is currently under maintenance."
                : "The system is running normally.";

        ApiResponse<MaintenanceResponse> response = ApiResponse.<MaintenanceResponse>builder()
                .message(message)
                .result(MaintenanceResponse.builder()
                        .maintenanceMode(isMaintenance)
                        .from(Instant.now())
                        .build())
                .build();
        return ResponseEntity.ok(response);
    }
}