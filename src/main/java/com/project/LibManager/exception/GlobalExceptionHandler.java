package com.project.LibManager.exception;

import com.project.LibManager.constant.ErrorCode;
import com.project.LibManager.dto.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse<Void>> handlingRuntimeException(RuntimeException runtimeException) {
        return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                .message(runtimeException.getMessage())
                .build());
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<Void>> handlingAppException(AppException appException) {
        ErrorCode errorCode = appException.getErrorCode();

        return ResponseEntity.status(errorCode.getStatusCode()).body(ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build());
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse<Void>> handlingAccessDeniedException(AccessDeniedException accessDeniedException) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.getStatusCode()).body(ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handlingVadidation(MethodArgumentNotValidException ex) {
        String enumKey = Optional.ofNullable(ex.getFieldError())
                .map(FieldError::getDefaultMessage)
                .orElse("UNKNOWN_ERROR");

        ErrorCode errorCode = ErrorCode.valueOf(enumKey);

        return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build());
    }

    @ExceptionHandler(value = AuthenticationServiceException.class)
    ResponseEntity<ApiResponse<Void>> handlingAuthenticationException(AuthenticationServiceException ex) {
        log.error("Authentication error: {}", ex.getMessage());
        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.getStatusCode()).body(ApiResponse.<Void>builder()
                .code(ErrorCode.UNAUTHORIZED.getCode())
                .message("Authentication failed: " + ex.getMessage())
                .build());
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handlingIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.<Void>builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Void>> handleNullPointerException(NullPointerException ex) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(500)
                .message("Lỗi hệ thống: " + ex.getMessage())
                .result(null)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
