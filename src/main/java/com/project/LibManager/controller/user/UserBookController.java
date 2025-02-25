package com.project.LibManager.controller.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.LibManager.dto.response.ApiResponse;
import com.project.LibManager.dto.response.BorrowingResponse;
import com.project.LibManager.service.IBookService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("user/books")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "JWT Authentication")
public class UserBookController {
    private final IBookService bookService;

    @PostMapping("/user/borrow/{bookId}") 
    public ResponseEntity<ApiResponse<BorrowingResponse>> borrowBooks(@PathVariable Long bookId) {
        ApiResponse<BorrowingResponse> response = ApiResponse.<BorrowingResponse>builder()
                                                             .message("Borrow book is successfully!")
                                                             .result(bookService.borrowBook(bookId))
                                                             .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/return/{bookId}") 
    public ResponseEntity<ApiResponse<BorrowingResponse>> returnBooks(@PathVariable Long bookId) {
        ApiResponse<BorrowingResponse> response = ApiResponse.<BorrowingResponse>builder()
                                                             .message("Return book is successfully!")
                                                             .result(bookService.returnBook(bookId))
                                                             .build();
        return ResponseEntity.ok(response);
    }
}
