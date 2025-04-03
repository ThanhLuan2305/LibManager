package com.project.libmanager.controller.common;

import com.project.libmanager.exception.AppException;
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

import com.project.libmanager.criteria.BookCriteria;
import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.BookResponse;
import com.project.libmanager.service.IBookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for retrieving book information.
 * Provides endpoints for fetching all books, a specific book by ID, and searching books with criteria.
 */
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
@Tag(name = "Book Management", description = "Endpoints for retrieving and searching books")
public class BookController {
    private final IBookService bookService;

    /**
     * Retrieves a paginated list of all books.
     *
     * @param offset the page number (starting from 0)
     * @param limit  the number of items per page
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a {@link Page} of {@link BookResponse} objects
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - invalid pagination parameters (ErrorCode.INVALID_INPUT)
     * @implNote Uses {@link Pageable} to fetch books from {@link IBookService} and wraps them in an {@link ApiResponse}.
     */
    @GetMapping
    @Operation(summary = "Get all books",
            description = "Retrieves a paginated list of all books.")
    @Parameter(name = "offset", description = "Page number (default: 0)")
    @Parameter(name = "limit", description = "Items per page (default: 10)")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getBooks(@RequestParam(defaultValue = "0") int offset,
                                                                    @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        ApiResponse<Page<BookResponse>> response = ApiResponse.<Page<BookResponse>>builder()
                .result(bookService.getBooks(pageable))
                .message("Books retrieved successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves details of a specific book by its ID.
     *
     * @param bookId the ID of the book to retrieve
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a {@link BookResponse} detailing the book
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - book not found (ErrorCode.BOOK_NOT_EXISTED)
     * @implNote Fetches the book from {@link IBookService} and returns the details in an {@link ApiResponse}.
     */
    @GetMapping("/{bookId}")
    @Operation(summary = "Get a book by ID",
            description = "Retrieves details of a specific book by its ID.")
    @Parameter(description = "ID of the book to retrieve")
    public ResponseEntity<ApiResponse<BookResponse>> getBook(@PathVariable Long bookId) {
        ApiResponse<BookResponse> response = ApiResponse.<BookResponse>builder()
                .result(bookService.getBook(bookId))
                .message("Book retrieved successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Searches for books based on specified criteria with pagination.
     *
     * @param criteria the search criteria for filtering books
     * @param pageable the pagination information
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a {@link Page} of {@link BookResponse} objects
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - invalid search criteria (ErrorCode.INVALID_INPUT)
     * @implNote Uses {@link BookCriteria} and {@link Pageable} to search books via {@link IBookService} and wraps the results in an {@link ApiResponse}.
     */
    @GetMapping("/search")
    @Operation(summary = "Search books",
            description = "Searches for books based on specified criteria with pagination.")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> searchBooks(@ParameterObject BookCriteria criteria,
                                                                       @ParameterObject Pageable pageable) {
        ApiResponse<Page<BookResponse>> response = ApiResponse.<Page<BookResponse>>builder()
                .message("Search book successfully")
                .result(bookService.searchBook(criteria, pageable))
                .build();
        return ResponseEntity.ok().body(response);
    }
}