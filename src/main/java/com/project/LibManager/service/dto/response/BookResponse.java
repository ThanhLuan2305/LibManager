package com.project.LibManager.service.dto.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookResponse {
    private Long id;

    private String isbn;

    private String title;

    private String author;

    private BookTypeResponse bookType;

    private int stock;

    private String publisher;

    private Instant publishedDate;

    private int maxBorrowDays;

    private String location;

    private String coverImageUrl;

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;
}
