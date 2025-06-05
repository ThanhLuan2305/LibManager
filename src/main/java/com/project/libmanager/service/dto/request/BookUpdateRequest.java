package com.project.libmanager.service.dto.request;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for updating an existing book")
public class BookUpdateRequest {
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    @Schema(description = "Title of the book", example = "The Great Gatsby")
    private String title;

    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    @Schema(description = "ISBN of the book", example = "9780743273565")
    private String isbn;

    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    @Schema(description = "Author of the book", example = "F. Scott Fitzgerald")
    private String author;

    @Schema(description = "ID of the book type", example = "1")
    private Long typeId;

    @Min(value = 1, message = "VALUE_OUT_OF_RANGE")
    @Schema(description = "Number of books in stock", example = "10")
    private int stock;

    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    @Schema(description = "Publisher of the book", example = "Scribner")
    private String publisher;

    @PastOrPresent(message = "BIRTH_DATE_MUST_BE_IN_PAST")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'HH:mm:ss.SSSX", timezone = "UTC")
    @Schema(description = "Publication date of the book", example = "10-04-1925T00:00:00.000Z")
    private Instant publishedDate;

    @Min(value = 1, message = "VALUE_OUT_OF_RANGE")
    @Schema(description = "Maximum number of days a book can be borrowed", example = "14")
    private int maxBorrowDays;

    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    @Schema(description = "Location of the book in the library", example = "Shelf A1")
    private String location;

    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    @Schema(description = "URL of the book's cover image", example = "https://example.com/cover.jpg")
    private String coverImageUrl;

    @Schema(description = "Whether the book is marked as deleted", example = "false")
    private boolean deleted;
}