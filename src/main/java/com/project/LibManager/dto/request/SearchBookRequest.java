package com.project.LibManager.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SearchBookRequest {
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    private String title;
    
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    private String author; 
    
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    private String typeName;

    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    private String publisher;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/d")
    @DateTimeFormat(pattern = "yyyy/MM/d") 
    private LocalDate publishedDateFrom;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/d")
    @DateTimeFormat(pattern = "yyyy/MM/d") 
    private LocalDate publishedDateTo;
    
    private int maxBorrowDays;
    
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    private String location;
    
    @Size(max = 255, message = "CHARACTER_LIMIT_EXCEEDED")
    private String nameUserBrrow;
}
