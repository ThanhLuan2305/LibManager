package com.project.LibManager.constant;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1003, "Invalid email address", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least 6 characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    ROLE_NOT_EXISTED(1008, "Role not existed", HttpStatus.NOT_FOUND),
    NOT_BLANK(1009, "Can not be blank", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_VERIFIED(1010, "Your email is not verified", HttpStatus.BAD_REQUEST),
    OTP_NOT_EXISTED(1011, "Otp not existed", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(1012, "OTP has expired", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH(1013, "Password not match", HttpStatus.BAD_REQUEST),
    PASSWORD_DUPLICATED(1014, "New password must be different from old password", HttpStatus.BAD_REQUEST),
    BOOK_NOT_EXISTED(1015, "Book not existed", HttpStatus.NOT_FOUND),
    BOOKTYPE_NOT_EXISTED(1016, "Book type not existed", HttpStatus.NOT_FOUND),
    BOOK_OUT_OF_STOCK(1017, "Book is out of stock", HttpStatus.BAD_REQUEST),
    BOOK_ALREADY_BORROWED(1018, "You have already borrowed this book", HttpStatus.BAD_REQUEST),
    BOOK_NOT_BORROWED(1019, "Book not borrowed", HttpStatus.NOT_FOUND),
    BOOK_RETURN_LATE(1020, "You have returned the book late!", HttpStatus.UNPROCESSABLE_ENTITY),
    CHARACTER_LIMIT_EXCEEDED(1021, "Character length must not exceed 255", HttpStatus.BAD_REQUEST),
    BIRTH_DATE_MUST_BE_IN_PAST(1022, "Birth date must be in past", HttpStatus.BAD_REQUEST),
    VALUE_OUT_OF_RANGE(1023, "Value is out of allowed range", HttpStatus.BAD_REQUEST),
    MAINTENACE_MODE(503,"The system is under maintenance. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE),
    BOOK_IS_CURRENTLY_BORROWED(1024,"This book is currently borrowed and cannot be deleted.", HttpStatus.BAD_REQUEST),
    BOOK_EXISTED(1025, "Book existed", HttpStatus.BAD_REQUEST),
    JWT_TOKEN_INVALID(1026, "JWT Token invalid", HttpStatus.BAD_REQUEST),
    FROMDATE_BEFORE_TODATE(1027, "Fromdate must be before todate", HttpStatus.BAD_REQUEST),
    ISBN_MUST_BE_13_CHARACTERS(1028, "ISBN must be 13 characters", HttpStatus.BAD_REQUEST),
    FILE_EMPTY(1029, "The file is empty.", HttpStatus.BAD_REQUEST),
    FILE_LIMIT(1030, "The file is too large. Maximum allowed size is 5MB.", HttpStatus.BAD_REQUEST),
    JWT_TOKEN_EXPIRED(1031, "JWT token has expired.", HttpStatus.UNAUTHORIZED),
    USER_IS_DELETED(1032, "User has been deleted", HttpStatus.NOT_FOUND),
    USER_HAS_OVERDUE_BOOKS(1033, "User has overdue books and must return them before borrowing new ones.", HttpStatus.BAD_REQUEST),
    USER_HAS_TOO_MANY_LATE_RETURNS(1034, "User has exceeded the maximum allowed late returns.", HttpStatus.BAD_REQUEST),
    USER_NOT_BORROW(1035, "The user has not borrowed any books.", HttpStatus.NOT_FOUND),

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