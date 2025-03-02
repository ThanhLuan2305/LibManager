package com.project.LibManager.controller.common;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.LibManager.criteria.BookCriteria;
import com.project.LibManager.dto.response.ApiResponse;
import com.project.LibManager.dto.response.BookResponse;
import com.project.LibManager.entity.Book;
import com.project.LibManager.service.IBookService;
import com.project.LibManager.specification.BookQueryService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
                .message("Books retrieved successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/detail/{bookId}")
    public ResponseEntity<ApiResponse<BookResponse>> getBook(@PathVariable Long bookId) {
        ApiResponse<BookResponse> response = ApiResponse.<BookResponse>builder()
                .result(bookService.getBook(bookId))
                .message("Book retrieved successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> searchBooks(@ParameterObject BookCriteria criteria,
            Pageable pageable) {
        Page<Book> books = bookQueryService.findByCriteria(criteria, pageable);
        Page<BookResponse> booksReponse = bookService.mapBookPageBookResponsePage(books);
        ApiResponse<Page<BookResponse>> response = ApiResponse.<Page<BookResponse>>builder()
                .message("Search book successfully")
                .result(booksReponse)
                .build();
        return ResponseEntity.ok().body(response);
    }
}
