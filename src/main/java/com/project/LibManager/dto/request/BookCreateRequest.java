package com.project.LibManager.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

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
public class BookCreateRequest {

    @NotBlank(message = "NOT_BLANK")
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    private String title;

    @NotBlank(message = "NOT_BLANK")
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    private String author;
    
    @NotBlank(message = "NOT_BLANK")
    @Size(min = 13, max = 13, message = "ISBN_MUST_BE_13_CHARACTERS")
    private String isbn;

    @NotNull(message = "NOT_BLANK")
    private Long typeId;

    @Min(value = 1, message = "VALUE_OUT_OF_RANGE")
    private int stock;  

    @NotBlank(message = "NOT_BLANK")
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    private String publisher;
    
    @NotNull(message = "NOT_BLANK")
    @PastOrPresent(message = "BIRTH_DATE_MUST_BE_IN_PAST")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/d")
    @DateTimeFormat(pattern = "yyyy/MM/d")
    private LocalDate publishedDate;

    @Min(value = 1, message = "VALUE_OUT_OF_RANGE")
    private int maxBorrowDays;

    @NotBlank(message = "NOT_BLANK")
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    private String location;

    @NotBlank(message = "NOT_BLANK")
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    private String coverImageUrl;

    @NotNull(message = "NOT_BLANK")
    private String isDeleted;

}
