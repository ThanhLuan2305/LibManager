package com.project.LibManager.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchUserRequest {
    private String fullName;
    
    private String email;

    private String role;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/d")
    @DateTimeFormat(pattern = "yyyy/MM/d") 
    private LocalDate fromDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/d")
    @DateTimeFormat(pattern = "yyyy/MM/d") 
    private LocalDate toDate;

    @AssertTrue(message = "FROMDATE_BEFORE_TODATE")
    public boolean isValidDateRange() {
        if (fromDate != null && toDate != null) {
            return fromDate.isBefore(toDate);
        }
        return true;
    }
}
