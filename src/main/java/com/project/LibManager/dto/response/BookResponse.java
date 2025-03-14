package com.project.LibManager.dto.response;

import java.time.LocalDate;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
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

    private LocalDate publishedDate;

    private int maxBorrowDays;

    private String location;
    
    private String coverImageUrl;

    private LocalDate createdAt;

    private LocalDate updatedAt;

    private String createdBy;

    private String updatedBy;
}
