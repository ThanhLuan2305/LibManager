package com.project.libmanager.controller.admin;

import java.util.Objects;
import java.util.Optional;

import com.project.libmanager.criteria.BookCriteria;
import com.project.libmanager.exception.AppException;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing books by admin users.
 * Provides endpoints for creating, updating, deleting, retrieving, searching, and importing books, as well as fetching borrowing history.
 */
@RestController
@RequestMapping("admin/books")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
@Tag(name = "Admin Book Management", description = "Endpoints for managing books by admin users")
public class AdminBookController {
    private final IBookService bookService;

    /**
     * Retrieves a paginated list of all books for admin users.
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
    @Operation(summary = "Get all books for admin",
            description = "Retrieves a paginated list of all books for admin users.")
    @Parameter(name = "offset", description = "Page number (default: 0)")
    @Parameter(name = "limit", description = "Items per page (default: 10)")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getBooks(@RequestParam(defaultValue = "0") int offset,
                                                                    @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        ApiResponse<Page<BookResponse>> response = ApiResponse.<Page<BookResponse>>builder()
                .result(bookService.getBooksForAdmin(pageable))
                .message("Books retrieved successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new book with the provided details.
     *
     * @param bookCreateRequest the request containing book creation details
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a {@link BookResponse} detailing the created book
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - invalid book data (ErrorCode.INVALID_INPUT)
     * @implNote Delegates book creation to {@link IBookService} and returns the book details in an {@link ApiResponse}.
     */
    @PostMapping
    @Operation(summary = "Create a new book",
            description = "Creates a new book with the provided details.")
    public ResponseEntity<ApiResponse<BookResponse>> createBook(
            @RequestBody @Valid BookCreateRequest bookCreateRequest) {
        BookResponse bookResponse = bookService.createBook(bookCreateRequest);
        ApiResponse<BookResponse> response = ApiResponse.<BookResponse>builder()
                .message("Create Book successfully")
                .result(bookResponse)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing book by its ID.
     *
     * @param bookUpdateRequest the request containing updated book details
     * @param id                the ID of the book to update
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a {@link BookResponse} detailing the updated book
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - book not found (ErrorCode.BOOK_NOT_EXISTED)
     *                      - invalid book data (ErrorCode.INVALID_INPUT)
     * @implNote Delegates book update to {@link IBookService} and returns the updated book details in an {@link ApiResponse}.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a book",
            description = "Updates an existing book by its ID.")
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

    /**
     * Deletes a book by its ID.
     *
     * @param id the ID of the book to delete
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a success message
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - book not found (ErrorCode.BOOK_NOT_EXISTED)
     * @implNote Delegates book deletion to {@link IBookService} and returns a success message in an {@link ApiResponse}.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book",
            description = "Deletes a book by its ID.")
    public ResponseEntity<ApiResponse<String>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Delete Book successfully")
                .result("success")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a paginated list of books borrowed by a specific user.
     *
     * @param userId the ID of the user whose borrowing history is to be retrieved
     * @param offset the page number (starting from 0)
     * @param limit  the number of items per page
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a {@link Page} of {@link BorrowingResponse} objects
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - user not found (ErrorCode.USER_NOT_EXISTED)
     *                      - invalid pagination parameters (ErrorCode.INVALID_INPUT)
     * @implNote Uses {@link Pageable} to fetch borrowed books from {@link IBookService} and wraps them in an {@link ApiResponse}.
     */
    @GetMapping("/borrow-by-user")
    @Operation(summary = "Get books borrowed by user",
            description = "Retrieves a paginated list of books borrowed by a specific user.")
    @Parameter(name = "userId", description = "ID of the user")
    @Parameter(name = "offset", description = "Page number (default: 0)")
    @Parameter(name = "limit", description = "Items per page (default: 10)")
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

    /**
     * Imports books from a CSV file.
     *
     * @param file the CSV file containing book data
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a success or error message
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - invalid file format (ErrorCode.INVALID_FILE_FORMAT)
     *                      - file processing fails (ErrorCode.FILE_PROCESSING_FAILED)
     * @implNote Delegates book import to {@link IBookService} and returns a success message in an {@link ApiResponse}.
     */
    @PostMapping("/import")
    @Operation(summary = "Import books from CSV",
            description = "Imports books from a CSV file.")
    @Parameter(name = "file", description = "CSV file containing book data")
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
    @Operation(summary = "Search books for admin",
            description = "Searches for books based on specified criteria with pagination.")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> searchBooks(@ParameterObject BookCriteria criteria,
                                                                       @ParameterObject Pageable pageable) {
        ApiResponse<Page<BookResponse>> response = ApiResponse.<Page<BookResponse>>builder()
                .message("Search book successfully")
                .result(bookService.searchBook(criteria, pageable))
                .build();
        return ResponseEntity.ok().body(response);
    }

    /**
     * Retrieves details of a specific book by its ID for admin users.
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
    @Operation(summary = "Get a book by ID for admin",
            description = "Retrieves details of a specific book by its ID for admin users.")
    @Parameter(description = "ID of the book to retrieve")
    public ResponseEntity<ApiResponse<BookResponse>> getBook(@PathVariable Long bookId) {
        ApiResponse<BookResponse> response = ApiResponse.<BookResponse>builder()
                .result(bookService.getBookForAdmin(bookId))
                .message("Book retrieved successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}