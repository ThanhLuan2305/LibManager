package com.project.libmanager.service.dto.request;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;

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
public class BookUpdateRequest {
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    private String title;

    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    private String isbn;

    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    private String author;

    Long typeId;

    @Min(value = 1, message = "VALUE_OUT_OF_RANGE")
    private int stock;

    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    private String publisher;

    @PastOrPresent(message = "BIRTH_DATE_MUST_BE_IN_PAST")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant publishedDate;

    @Min(value = 1, message = "VALUE_OUT_OF_RANGE")
    private int maxBorrowDays;

    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    private String location;

    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    private String coverImageUrl;

    private boolean deleted;
}