package com.project.libmanager.service.dto.request;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Request body for creating a new book")
public class BookCreateRequest {

    @NotBlank(message = "NOT_BLANK")
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    @Schema(description = "Title of the book", example = "The Great Gatsby")
    private String title;

    @NotBlank(message = "NOT_BLANK")
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    @Schema(description = "Author of the book", example = "F. Scott Fitzgerald")
    private String author;

    @NotBlank(message = "NOT_BLANK")
    @Size(min = 13, max = 13, message = "ISBN_MUST_BE_13_CHARACTERS")
    @Schema(description = "ISBN of the book, must be exactly 13 characters", example = "9780743273565")
    private String isbn;

    @NotNull(message = "NOT_BLANK")
    @Schema(description = "ID of the book type", example = "1")
    private Long typeId;

    @Min(value = 1, message = "VALUE_OUT_OF_RANGE")
    @Schema(description = "Number of books in stock", example = "10")
    private int stock;

    @NotBlank(message = "NOT_BLANK")
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    @Schema(description = "Publisher of the book", example = "Scribner")
    private String publisher;

    @NotNull(message = "NOT_BLANK")
    @PastOrPresent(message = "BIRTH_DATE_MUST_BE_IN_PAST")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'HH:mm:ss.SSSX", timezone = "UTC")
    @Schema(description = "Publication date of the book", example = "10-04-1925T00:00:00.000Z")
    private Instant publishedDate;

    @Min(value = 1, message = "VALUE_OUT_OF_RANGE")
    @Schema(description = "Maximum number of days a book can be borrowed", example = "14")
    private int maxBorrowDays;

    @NotBlank(message = "NOT_BLANK")
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    @Schema(description = "Location of the book in the library", example = "Shelf A1")
    private String location;

    @NotBlank(message = "NOT_BLANK")
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    @Schema(description = "URL of the book's cover image", example = "https://example.com/cover.jpg")
    private String coverImageUrl;

    @NotNull(message = "NOT_BLANK")
    @Schema(description = "Whether the book is marked as deleted", example = "false")
    private boolean deleted;
}