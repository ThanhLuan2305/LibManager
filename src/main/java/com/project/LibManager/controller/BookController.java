package com.project.LibManager.controller;

import java.util.Map;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.LibManager.dto.request.BookCreateRequest;
import com.project.LibManager.dto.request.BookUpdateRequest;
import com.project.LibManager.dto.request.BorrowingRequest;
import com.project.LibManager.dto.request.SearchBookRequest;
import com.project.LibManager.dto.response.ApiResponse;
import com.project.LibManager.dto.response.BookResponse;
import com.project.LibManager.dto.response.BorrowingResponse;
import com.project.LibManager.service.IBookService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
public class BookController {
    private final IBookService bookService;

    @GetMapping
    public ApiResponse<Page<BookResponse>> getBooks(@RequestParam(defaultValue = "0") int offset,
                                            @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return ApiResponse.<Page<BookResponse>>builder()
                .result(bookService.getBooks(pageable))
                .build();
    }

    @GetMapping("/{bookId}")
    public ApiResponse<BookResponse> getBook(@PathVariable Long bookId) {
        return ApiResponse.<BookResponse>builder()
                .result(bookService.getBook(bookId))
                .build();
    }

    @PostMapping
    public ApiResponse<BookResponse> createBook(@RequestBody @Valid BookCreateRequest bookCreateRequest) {
        return ApiResponse.<BookResponse>builder()
                .message("Create Book successfully")
                .result(bookService.createBook(bookCreateRequest))
                .build();
    }

    @PutMapping("{id}")
    public ApiResponse<BookResponse> updateBook(@RequestBody @Valid BookUpdateRequest bookUpdateRequest, @PathVariable Long id) {
        return ApiResponse.<BookResponse>builder()
                .message("Update Book successfully")
                .result(bookService.updateBook(bookUpdateRequest, id))
                .build();
    }
    
    @DeleteMapping("{id}")
    public ApiResponse<String> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ApiResponse.<String>builder()
                .message("Delete Book successfully")
                .build();
    }

    @PostMapping("/search") 
    public ApiResponse<Page<BookResponse>> searchBooks(@RequestBody @Valid SearchBookRequest searchBookRequest,
                                               @RequestParam(defaultValue = "0") int offset,
                                               @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return ApiResponse.<Page<BookResponse>>builder()
                .result(bookService.searchBooks(searchBookRequest, pageable))
                .build();
    }

    @PostMapping("/borrow") 
    public ApiResponse<BorrowingResponse> borrowBooks(@RequestBody BorrowingRequest borrowingRequest) {
        return ApiResponse.<BorrowingResponse>builder()
                .message("Borrow book is successfully!")
                .result(bookService.borrowBook(borrowingRequest))
                .build();
    }

    @PostMapping("/return") 
    public ApiResponse<BorrowingResponse> returnBooks(@RequestBody BorrowingRequest borrowingRequest) {
        return ApiResponse.<BorrowingResponse>builder()
                .message("Return book is successfully!")
                .result(bookService.returnBook(borrowingRequest))
                .build();
    }

    @GetMapping("/borrow-by-user")
    public ApiResponse<Page<BookResponse>> getBookBorrowByUser(@RequestParam Long userId, 
                                                        @RequestParam(defaultValue = "0") int offset,
                                                        @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return ApiResponse.<Page<BookResponse>>builder()
                .result(bookService.getBookBorrowByUser(userId, pageable))
                .build();
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importBooks(@RequestParam("file") MultipartFile file) {
        if (!Objects.equals(file.getContentType(), "text/csv") && !file.getOriginalFilename().endsWith(".csv")) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(Map.of(
                "status", HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "error", "UNSUPPORTED_MEDIA_TYPE",
                "message", "Only CSV files are supported."
            ));
        }

        bookService.importBooks(file);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "status", HttpStatus.OK.value(),
            "message", "Books imported successfully!"
        ));
    }
}

