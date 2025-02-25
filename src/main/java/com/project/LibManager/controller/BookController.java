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

import com.project.LibManager.criteria.BookCriteria;
import com.project.LibManager.dto.request.BookCreateRequest;
import com.project.LibManager.dto.request.BookUpdateRequest;
import com.project.LibManager.dto.response.ApiResponse;
import com.project.LibManager.dto.response.BookResponse;
import com.project.LibManager.dto.response.BorrowingResponse;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.entity.Book;
import com.project.LibManager.service.IBookService;
import com.project.LibManager.specification.BookQueryService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
public class BookController {
    private final IBookService bookService;
    private final BookQueryService bookQueryService;


    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getBooks(@RequestParam(defaultValue = "0") int offset,
                                                   @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        ApiResponse<Page<BookResponse>> response = ApiResponse.<Page<BookResponse>>builder()
                                                              .result(bookService.getBooks(pageable))
                                                              .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/detail/{bookId}")
    public ResponseEntity<ApiResponse<BookResponse>> getBook(@PathVariable Long bookId) {
        ApiResponse<BookResponse> response = ApiResponse.<BookResponse>builder()
                                                        .result(bookService.getBook(bookId))
                                                        .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin")
    public ResponseEntity<ApiResponse<BookResponse>> createBook(@RequestBody @Valid BookCreateRequest bookCreateRequest) {
        ApiResponse<BookResponse> response = ApiResponse.<BookResponse>builder()
                                                        .message("Create Book successfully")
                                                        .result(bookService.createBook(bookCreateRequest))
                                                        .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(@RequestBody @Valid BookUpdateRequest bookUpdateRequest, @PathVariable Long id) {
        ApiResponse<BookResponse> response = ApiResponse.<BookResponse>builder()
                                                        .message("Update Book successfully")
                                                        .result(bookService.updateBook(bookUpdateRequest, id))
                                                        .build();
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        ApiResponse<String> response = ApiResponse.<String>builder()
                                                 .message("Delete Book successfully")
                                                 .build();
        return ResponseEntity.ok(response);
    }

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

    @GetMapping("/admin/borrow-by-user")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getBookBorrowByUser(@RequestParam Long userId, 
                                                          @RequestParam(defaultValue = "0") int offset,
                                                          @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        ApiResponse<Page<BookResponse>> response = ApiResponse.<Page<BookResponse>>builder()
                                                              .result(bookService.getBookBorrowByUser(userId, pageable))
                                                              .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/import")
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

    @GetMapping("/admin/search")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getAllBooks(BookCriteria criteria, Pageable pageable) {
        Page<Book> books = bookQueryService.findByCriteria(criteria, pageable);
        Page<BookResponse> booksReponse = bookService.mapBookPageBookResponsePage(books);
        ApiResponse<Page<BookResponse>> response = ApiResponse.<Page<BookResponse>>builder()
                .message("Search book successfully")
                .result(booksReponse)
                .build();
        return ResponseEntity.ok().body(response);
    }
}

