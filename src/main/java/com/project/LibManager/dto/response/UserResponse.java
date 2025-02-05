package com.project.LibManager.dto.response;

import java.time.LocalDate;

import lombok.Data;

@Data
public class UserResponse {
	String id;
	String email;
	String password;
    String fullName;
    LocalDate birthDate;
    Boolean isVerified = false;
}
