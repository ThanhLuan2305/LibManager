package com.project.LibManager.exception;

import com.project.LibManager.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;



@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(RuntimeException runtimeException) {
    	ApiResponse apiRespones = new ApiResponse();
        apiRespones.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiRespones.setMessage(runtimeException.getMessage());
        return ResponseEntity.badRequest().body(apiRespones);
    }
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException appException) {
        ErrorCode errorCode = appException.getErrorCode();
        ApiResponse apiRespones = new ApiResponse();
        
        apiRespones.setCode(errorCode.getCode());
        apiRespones.setMessage(errorCode.getMessage());
        
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiRespones);
    }
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingVadidation(MethodArgumentNotValidException ex) {
        String enumKey = ex.getFieldError().getDefaultMessage();

        ErrorCode errorCode = ErrorCode.valueOf(enumKey);

        ApiResponse apiRespones = new ApiResponse();
        apiRespones.setCode(errorCode.getCode());
        apiRespones.setMessage(errorCode.getMessage());
        return ResponseEntity.badRequest().body(apiRespones);
    }
}
