package com.project.LibManager.dto.response;

import java.time.LocalDate;


import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BorrowingResponse {
    Long id;

    UserResponse user;

    BookResponse book;

    LocalDate borrowDate;

    LocalDate dueDate;

    @Column
    LocalDate returnDate;
}
