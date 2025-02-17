package com.project.LibManager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookResponse {
    private Long id;

    private String isbn;

    private String title;

    private String author;

    private BookTypeResponse bookType;

    private int stock;

    private String publisher;

    private String publishedDate;

    private int maxBorrowDays;

    private String location;
    
    private String coverImageUrl;
}
