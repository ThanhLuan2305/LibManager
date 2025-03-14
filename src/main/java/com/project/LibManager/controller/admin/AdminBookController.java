package com.project.LibManager.controller.admin;

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
import com.project.LibManager.dto.response.ApiResponse;
import com.project.LibManager.dto.response.BookResponse;
import com.project.LibManager.service.IBookService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("admin/books")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
public class AdminBookController {
    private final IBookService bookService;

    @PostMapping("")
    public ResponseEntity<ApiResponse<BookResponse>> createBook(@RequestBody @Valid BookCreateRequest bookCreateRequest) {
        BookResponse bookResponse = bookService.createBook(bookCreateRequest);
        ApiResponse<BookResponse> response = ApiResponse.<BookResponse>builder()
                .message("Create Book successfully")
                .result(bookResponse)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(@RequestBody @Valid BookUpdateRequest bookUpdateRequest, 
                                                                @PathVariable Long id) {
        BookResponse bookResponse = bookService.updateBook(bookUpdateRequest, id);
        ApiResponse<BookResponse> response = ApiResponse.<BookResponse>builder()
                .message("Update Book successfully")
                .result(bookResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Delete Book successfully")
                .result("success")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/borrow-by-user")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getBookBorrowByUser(@RequestParam Long userId, 
                                                                               @RequestParam(defaultValue = "0") int offset,
                                                                               @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        Page<BookResponse> bookPage = bookService.getBookBorrowByUser(userId, pageable);
        ApiResponse<Page<BookResponse>> response = ApiResponse.<Page<BookResponse>>builder()
                .message("Fetched books borrowed by user successfully")
                .result(bookPage)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<String>> importBooks(@RequestParam("file") MultipartFile file) {
        if (!Objects.equals(file.getContentType(), "text/csv") && !file.getOriginalFilename().endsWith(".csv")) {
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .message("Only CSV files are supported.")
                    .result("error")
                    .build();
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
        }

        bookService.importBooks(file);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Books imported successfully!")
                .result("success")
                .build();
        return ResponseEntity.ok(response);
    }
}
