package com.project.libmanager.service.dto.response;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Response containing book details")
public class BookResponse {
    @Schema(description = "Unique identifier of the book", example = "1")
    private Long id;

    @Schema(description = "ISBN of the book", example = "9780743273565")
    private String isbn;

    @Schema(description = "Title of the book", example = "The Great Gatsby")
    private String title;

    @Schema(description = "Author of the book", example = "F. Scott Fitzgerald")
    private String author;

    @Schema(description = "Type of the book")
    private BookTypeResponse bookType;

    @Schema(description = "Number of books in stock", example = "10")
    private int stock;

    @Schema(description = "Publisher of the book", example = "Scribner")
    private String publisher;

    @Schema(description = "Publication date of the book", example = "1925-04-10T00:00:00.000Z")
    private Instant publishedDate;

    @Schema(description = "Maximum number of days the book can be borrowed", example = "14")
    private int maxBorrowDays;

    @Schema(description = "Location of the book in the library", example = "Shelf A1")
    private String location;

    @Schema(description = "URL of the book's cover image", example = "https://example.com/cover.jpg")
    private String coverImageUrl;

    @Schema(description = "Whether the book is marked as deleted", example = "false")
    private boolean deleted;

    @Schema(description = "Creation timestamp of the book record", example = "2025-04-02T12:00:00.000Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp of the book record", example = "2025-04-02T12:00:00.000Z")
    private Instant updatedAt;

    @Schema(description = "User who created the book record", example = "admin")
    private String createdBy;

    @Schema(description = "User who last updated the book record", example = "admin")
    private String updatedBy;
}