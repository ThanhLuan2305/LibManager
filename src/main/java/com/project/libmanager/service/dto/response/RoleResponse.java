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
@Schema(description = "Response containing role details")
public class RoleResponse {
    @Schema(description = "Unique identifier of the role", example = "1")
    private Long id;

    @Schema(description = "Name of the role", example = "ADMIN")
    private String name;

    @Schema(description = "Description of the role", example = "Administrator role with full access")
    private String description;

    @Schema(description = "Creation timestamp of the role record", example = "2025-04-02T12:00:00.000Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp of the role record", example = "2025-04-02T12:00:00.000Z")
    private Instant updatedAt;

    @Schema(description = "User who created the role record", example = "admin")
    private String createdBy;

    @Schema(description = "User who last updated the role record", example = "admin")
    private String updatedBy;
}