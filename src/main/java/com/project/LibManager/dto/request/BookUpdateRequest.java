package com.project.LibManager.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookUpdateRequest {
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    String title;

    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    String author;

    Long typeId;

    @Min(value = 1, message = "VALUE_OUT_OF_RANGE")
    int stock;  

    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    String publisher;
    
    @PastOrPresent(message = "BIRTH_DATE_MUST_BE_IN_PAST")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/d")
    @DateTimeFormat(pattern = "yyyy/MM/d")
    LocalDate publishedDate;

    @Min(value = 1, message = "VALUE_OUT_OF_RANGE")
    int maxBorrowDays;

    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    String location;

    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    String coverImageUrl;
}