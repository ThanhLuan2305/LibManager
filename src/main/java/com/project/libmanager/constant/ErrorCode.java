package com.project.libmanager.constant;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    JWT_HAVE_ISSUE(9998, "JWT have a problem when decoder", HttpStatus.UNAUTHORIZED),
    LOGIN_ERROR(9997, "Login failed, please double-check your email and password. ", HttpStatus.UNAUTHORIZED),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1003, "Invalid email address", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "The password must be at least 8 characters long and include letters, numbers, and special characters!", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    ROLE_NOT_EXISTED(1008, "Role not existed", HttpStatus.NOT_FOUND),
    NOT_BLANK(1009, "Can not be blank", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_VERIFIED(1010, "Your email is not verified", HttpStatus.BAD_REQUEST),
    OTP_NOT_EXISTED(1011, "Otp not existed", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(1012, "OTP has expired", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH(1013, "New password and confirm password are not match", HttpStatus.BAD_REQUEST),
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
    MAINTENACE_MODE(503, "The system is under maintenance. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE),
    BOOK_IS_CURRENTLY_BORROWED(1024, "This book is currently borrowed and cannot be deleted.", HttpStatus.BAD_REQUEST),
    BOOK_EXISTED(1025, "Book existed", HttpStatus.BAD_REQUEST),
    JWT_TOKEN_INVALID(1026, "JWT Token invalid", HttpStatus.BAD_REQUEST),
    FROMDATE_BEFORE_TODATE(1027, "Fromdate must be before todate", HttpStatus.BAD_REQUEST),
    ISBN_MUST_BE_13_CHARACTERS(1028, "ISBN must be 13 characters", HttpStatus.BAD_REQUEST),
    FILE_EMPTY(1029, "The file is empty.", HttpStatus.BAD_REQUEST),
    FILE_LIMIT(1030, "The file is too large. Maximum allowed size is 5MB.", HttpStatus.BAD_REQUEST),
    JWT_TOKEN_EXPIRED(1031, "JWT token has expired.", HttpStatus.UNAUTHORIZED),
    USER_IS_DELETED(1032, "User has been deleted", HttpStatus.NOT_FOUND),
    USER_HAS_OVERDUE_BOOKS(1033, "User has overdue books and must return them before borrowing new ones.",
            HttpStatus.BAD_REQUEST),
    USER_HAS_TOO_MANY_LATE_RETURNS(1034, "User has exceeded the maximum allowed late returns.", HttpStatus.BAD_REQUEST),
    USER_NOT_BORROW(1035, "The user has not borrowed any books.", HttpStatus.NOT_FOUND),
    LOGOUT_FAIL(1036, "Logout fail!.", HttpStatus.NOT_FOUND),
    MAIL_EXISTED(1037, "Mail existed", HttpStatus.BAD_REQUEST),
    USER_NEED_CHANGE_PASSWORD(1038, "User must change password before logging in", HttpStatus.FORBIDDEN),
    JTI_TOKEN_EXISTED(1039, "JTI Token is existed", HttpStatus.BAD_REQUEST),
    LOGINDETAIL_NOTFOUND(1040, "Don't found login detail", HttpStatus.NOT_FOUND),
    PHONE_INVALID(1041, "Phone number must be at least 10 characters ", HttpStatus.BAD_REQUEST),
    OTP_INVALID(1042, "OTP is invalid or expired", HttpStatus.BAD_REQUEST),
    OTP_IS_DULICATED(1043, "OTP is duplicated", HttpStatus.BAD_REQUEST),
    USER_NOT_VERIFIED(1044, "User has not verified email or phone number", HttpStatus.FORBIDDEN),
    PHONE_EXISTED(1045, "Phone existed", HttpStatus.BAD_REQUEST),
    OLD_PHONE_NOT_EXISTED(1045, "Old phone not existed", HttpStatus.NOT_FOUND),
    OLD_PHONE_INVALID(1045, "Old phone is invalid", HttpStatus.BAD_REQUEST),
    BORROW_NOT_FOUND(1046, "Don't found borrow book", HttpStatus.NOT_FOUND),
    CANNOT_DELETE_ADMIN(1047, "You can not delete admin!", HttpStatus.BAD_REQUEST),
    CANNOT_UPDATE_ADMIN(1048, "You can not update admin!", HttpStatus.BAD_REQUEST),
    BOOK_IS_DELETED(1049, "Book is deleted!", HttpStatus.BAD_REQUEST),
    BOOK_IS_BORROW(1050, "Book is currently borrowed!", HttpStatus.BAD_REQUEST),
    USER_CANNOT_BE_DELETED(1051, "User is currently borrowing books, cannot delete!", HttpStatus.BAD_REQUEST),
    ACTIVITY_LOG_NOT_EXISTED(1051, "Activity log not existed!", HttpStatus.NOT_FOUND),
    USER_BORROWING_RESTRICTED(1052, "User is restricted from borrowing due to three late returns!", HttpStatus.FORBIDDEN),
    ROOM_EXISTED(1053, "Room with this roomId already exists.", HttpStatus.BAD_REQUEST),
    ROOM_NOT_EXISTED(1053, "Room with this roomId not exists.", HttpStatus.NOT_FOUND),
    ROOM_IS_PRIVATE(1053, "Room private only existed two user in the room.", HttpStatus.NOT_FOUND),
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