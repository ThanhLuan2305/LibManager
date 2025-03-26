package com.project.libmanager.controller.common;

import com.project.libmanager.service.IMaintenanceService;
import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.MaintenanceResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
public class MaintenanceModeController {
    private final IMaintenanceService maintenanceService;

    @GetMapping("/maintenance/status")
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
