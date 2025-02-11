package com.project.LibManager.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

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
    String title;
    String author; 
    String typeName; 
    String publisher; 
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/d")
    @DateTimeFormat(pattern = "yyyy/MM/d") 
    LocalDate publishedDateFrom;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/d")
    @DateTimeFormat(pattern = "yyyy/MM/d") 
    LocalDate publishedDateTo;
    int maxBorrowDays;
    String location;
    String nameUserBrrow;
}
