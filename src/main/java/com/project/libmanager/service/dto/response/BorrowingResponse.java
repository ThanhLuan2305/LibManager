package com.project.libmanager.service.dto.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowingResponse {
    private Long id;

    private UserResponse user;

    private BookResponse book;

    private Instant borrowDate;

    private Instant dueDate;

    private Instant returnDate;

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;
}
