package com.project.LibManager.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchUserRequest {
    String fullName;
    String email;
    String role;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/d")
    @DateTimeFormat(pattern = "yyyy/MM/d") 
    LocalDate fromDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/d")
    @DateTimeFormat(pattern = "yyyy/MM/d") 
    LocalDate toDate;
}
