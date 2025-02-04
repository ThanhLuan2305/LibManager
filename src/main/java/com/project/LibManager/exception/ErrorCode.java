package com.project.LibManager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ErrorCode {
	UNCATEGORIZED_EXCEPTION(404, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    ;
	private int code;
    private String message;
    private final HttpStatusCode statusCode;
    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
	public int getCode() {
		return code;
	}
	public String getMessage() {
		return message;
	}
	public HttpStatusCode getStatusCode() {
		return statusCode;
	}
    
    
}