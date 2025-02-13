package com.project.LibManager.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookResponse {
    Long id;
    String isbn;
    String title;
    String author;
    BookTypeResponse bookType;
    int stock;
    String publisher;
    String publishedDate;
    int maxBorrowDays;
    String location;
    String coverImageUrl;
}
