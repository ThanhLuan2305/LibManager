package com.project.LibManager.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class BookCreateRequest {

    @NotBlank(message = "NOT_BLANK")
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    String title;

    @NotBlank(message = "NOT_BLANK")
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    String author;

    @NotNull(message = "NOT_BLANK")
    Long typeId;

    @Min(value = 1, message = "VALUE_OUT_OF_RANGE")
    int stock;  

    @NotBlank(message = "NOT_BLANK")
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    String publisher;
    
    @NotNull(message = "NOT_BLANK")
    @PastOrPresent(message = "BIRTH_DATE_MUST_BE_IN_PAST")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/d")
    @DateTimeFormat(pattern = "yyyy/MM/d")
    LocalDate publishedDate;

    @Min(value = 1, message = "VALUE_OUT_OF_RANGE")
    int maxBorrowDays;

    @NotBlank(message = "NOT_BLANK")
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    String location;

    @NotBlank(message = "NOT_BLANK")
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    String coverImageUrl;

}
