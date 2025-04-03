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
@Schema(description = "Response containing book type details")
public class BookTypeResponse {
    @Schema(description = "Unique identifier of the book type", example = "1")
    private Long id;

    @Schema(description = "Name of the book type", example = "Fiction")
    private String name;

    @Schema(description = "Creation timestamp of the book type record", example = "2025-04-02T12:00:00.000Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp of the book type record", example = "2025-04-02T12:00:00.000Z")
    private Instant updatedAt;

    @Schema(description = "User who created the book type record", example = "admin")
    private String createdBy;

    @Schema(description = "User who last updated the book type record", example = "admin")
    private String updatedBy;
}