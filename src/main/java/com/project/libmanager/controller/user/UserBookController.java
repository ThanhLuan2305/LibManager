package com.project.libmanager.controller.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.BorrowingResponse;
import com.project.libmanager.service.IBookService;

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

    @PostMapping("/borrow/{bookId}")
    public ResponseEntity<ApiResponse<BorrowingResponse>> borrowBooks(@PathVariable Long bookId) {
        ApiResponse<BorrowingResponse> response = ApiResponse.<BorrowingResponse>builder()
                .message("Borrow book is successfully!")
                .result(bookService.borrowBook(bookId))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/return/{bookId}")
    public ResponseEntity<ApiResponse<BorrowingResponse>> returnBooks(@PathVariable Long bookId) {
        ApiResponse<BorrowingResponse> response = ApiResponse.<BorrowingResponse>builder()
                .message("Return book is successfully!")
                .result(bookService.returnBook(bookId))
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/books-borrow")
    public ResponseEntity<ApiResponse<Page<BorrowingResponse>>> getBookBorrow(@RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        ApiResponse<Page<BorrowingResponse>> response = ApiResponse.<Page<BorrowingResponse>>builder()
                .message("List of borrowed books.")
                .result(bookService.getBookBorrowForUser(pageable))
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/books-return")
    public ResponseEntity<ApiResponse<Page<BorrowingResponse>>> getBookReturn(@RequestParam(defaultValue = "0") int offset,
                                                                         @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        ApiResponse<Page<BorrowingResponse>> response = ApiResponse.<Page<BorrowingResponse>>builder()
                .message("List of borrowed books.")
                .result(bookService.getBookReturnForUser(pageable))
                .build();
        return ResponseEntity.ok(response);
    }
}
