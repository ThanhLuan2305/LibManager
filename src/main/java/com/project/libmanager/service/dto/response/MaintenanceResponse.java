package com.project.libmanager.service.dto.response;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing maintenance mode details")
public class MaintenanceResponse {
    @Schema(description = "Whether the system is in maintenance mode", example = "true")
    private boolean maintenanceMode;

    @Schema(description = "Start time of the maintenance period", example = "2025-04-02T12:00:00.000Z")
    private Instant from;
}