package com.project.libmanager.controller.admin;

import java.util.Objects;
import java.util.Optional;

import com.project.libmanager.criteria.BookCriteria;
import com.project.libmanager.service.dto.response.BorrowingResponse;
import org.springdoc.core.annotations.ParameterObject;
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

import com.project.libmanager.service.dto.request.BookCreateRequest;
import com.project.libmanager.service.dto.request.BookUpdateRequest;
import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.BookResponse;
import com.project.libmanager.service.IBookService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("admin/books")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
public class AdminBookController {
    private final IBookService bookService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getBooks(@RequestParam(defaultValue = "0") int offset,
                                                                    @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        ApiResponse<Page<BookResponse>> response = ApiResponse.<Page<BookResponse>>builder()
                .result(bookService.getBooksForAdmin(pageable))
                .message("Books retrieved successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> createBook(
            @RequestBody @Valid BookCreateRequest bookCreateRequest) {
        BookResponse bookResponse = bookService.createBook(bookCreateRequest);
        ApiResponse<BookResponse> response = ApiResponse.<BookResponse>builder()
                .message("Create Book successfully")
                .result(bookResponse)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @RequestBody @Valid BookUpdateRequest bookUpdateRequest,
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
    public ResponseEntity<ApiResponse<Page<BorrowingResponse>>> getBookBorrowByUser(@RequestParam Long userId,
                                                                                    @RequestParam(defaultValue = "0") int offset,
                                                                                    @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        Page<BorrowingResponse> bookPage = bookService.getBookBorrowByUser(userId, pageable);
        ApiResponse<Page<BorrowingResponse>> response = ApiResponse.<Page<BorrowingResponse>>builder()
                .message("Fetched books borrowed by user successfully")
                .result(bookPage)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<String>> importBooks(@RequestParam("file") MultipartFile file) {
        boolean checkCSV = Optional.ofNullable(file.getOriginalFilename())
                .map(name -> name.endsWith(".csv"))
                .orElse(false);

        if (!Objects.equals(file.getContentType(), "text/csv") && !checkCSV) {
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

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> searchBooks(@ParameterObject BookCriteria criteria,
                                                                       Pageable pageable) {
        ApiResponse<Page<BookResponse>> response = ApiResponse.<Page<BookResponse>>builder()
                .message("Search book successfully")
                .result(bookService.searchBook(criteria, pageable))
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookResponse>> getBook(@PathVariable Long bookId) {
        ApiResponse<BookResponse> response = ApiResponse.<BookResponse>builder()
                .result(bookService.getBookForAdmin(bookId))
                .message("Book retrieved successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
