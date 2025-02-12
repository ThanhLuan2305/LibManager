package com.project.LibManager.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchBookRequest {
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    String title;
    
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    String author; 
    
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    String typeName;

    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    String publisher;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/d")
    @DateTimeFormat(pattern = "yyyy/MM/d") 
    LocalDate publishedDateFrom;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/d")
    @DateTimeFormat(pattern = "yyyy/MM/d") 
    LocalDate publishedDateTo;
    int maxBorrowDays;
    
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    String location;
    
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    String nameUserBrrow;
}
