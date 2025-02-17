package com.project.LibManager.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.LibManager.dto.response.ApiResponse;
import com.project.LibManager.dto.response.MaintenanceResponse;
import com.project.LibManager.service.IMaintenanceService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
public class MaintenanceController {
    private final IMaintenanceService maintenanceService;
    @PostMapping("/maintenance/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> setMaintenanceMode(@PathVariable boolean status) {
        maintenanceService.setMaintenanceMode(status);
        return ApiResponse.<Void>builder()
                .message("Server in Maintenance: " +status )
                .build();
    }

    @GetMapping("/maintenance/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MaintenanceResponse> getMaintenanceMode() {
        boolean isMaintenance = maintenanceService.isMaintenanceMode();
        
        String message = isMaintenance 
                            ? "The system is currently under maintenance." 
                            : "The system is running normally.";

        return ApiResponse.<MaintenanceResponse>builder()
                .message(message)
                .result(MaintenanceResponse.builder()
                                        .maintenanceMode(isMaintenance)
                                        .from(LocalDate.now())
                                        .build())
                .build();
    }

}
