package com.project.libmanager.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.IMaintenanceService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("admin/config")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
public class MaintenanceController {
    private final IMaintenanceService maintenanceService;

    @PostMapping("/maintenance/{status}")
    public ResponseEntity<ApiResponse<String>> setMaintenanceMode(@PathVariable boolean status) {
        maintenanceService.setMaintenanceMode(status);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Server in Maintenance: " + status)
                .result("success")
                .build();
        return ResponseEntity.ok(response);
    }
}
