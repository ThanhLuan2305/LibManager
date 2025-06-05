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
@Schema(description = "Response containing borrowing details")
public class BorrowingResponse {
    @Schema(description = "Unique identifier of the borrowing record", example = "1")
    private Long id;

    @Schema(description = "User who borrowed the book")
    private UserResponse user;

    @Schema(description = "Book being borrowed")
    private BookResponse book;

    @Schema(description = "Date the book was borrowed", example = "2025-04-02T12:00:00.000Z")
    private Instant borrowDate;

    @Schema(description = "Due date for returning the book", example = "2025-04-16T12:00:00.000Z")
    private Instant dueDate;

    @Schema(description = "Date the book was returned", example = "2025-04-10T12:00:00.000Z", nullable = true)
    private Instant returnDate;

    @Schema(description = "Creation timestamp of the borrowing record", example = "2025-04-02T12:00:00.000Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp of the borrowing record", example = "2025-04-02T12:00:00.000Z")
    private Instant updatedAt;

    @Schema(description = "User who created the borrowing record", example = "admin")
    private String createdBy;

    @Schema(description = "User who last updated the borrowing record", example = "admin")
    private String updatedBy;
}