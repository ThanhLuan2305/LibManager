package com.project.libmanager.exception;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.service.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

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
        log.error("Handling AppException: Code={}, Message={}", appException.getErrorCode().getCode(),
                appException.getErrorCode().getMessage());
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
        log.error("Field Errors: {}", ex.getBindingResult().getFieldErrors());
        log.error("Global Errors: {}", ex.getBindingResult().getGlobalErrors());

        List<String> errorMessages = new ArrayList<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorMessages.add(fieldError.getDefaultMessage());
        }

        for (ObjectError globalError : ex.getBindingResult().getGlobalErrors()) {
            errorMessages.add(globalError.getDefaultMessage());
        }

        String errorMessage = errorMessages.isEmpty() ? "UNCATEGORIZED_EXCEPTION" : errorMessages.get(0);

        ErrorCode errorCode = ErrorCode.valueOf(errorMessage);

        return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build());
    }

    @ExceptionHandler(value = AuthenticationServiceException.class)
    ResponseEntity<ApiResponse<Void>> handlingAuthenticationException(AuthenticationServiceException ex) {
        log.error("Authentication error: {}", ex.getMessage());
        return ResponseEntity.status(ErrorCode.LOGIN_ERROR.getStatusCode()).body(ApiResponse.<Void>builder()
                .code(ErrorCode.LOGIN_ERROR.getCode())
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
                .message("Error system: " + ex.getMessage())
                .result(null)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(value = ParseException.class)
    ResponseEntity<ApiResponse<Void>> handlingParseException(ParseException ex) {
        log.error("Failed to parse JWT token: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.<Void>builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message("Invalid token format: " + ex.getMessage())
                .build());
    }
}
