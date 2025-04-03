package com.project.libmanager.controller.user;

import com.project.libmanager.exception.AppException;
import com.project.libmanager.service.IBookService;
import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.BorrowingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * REST controller for managing book borrowing and returning operations for authenticated users.
 * Provides endpoints for borrowing, returning, and retrieving borrowing history. All operations require JWT authentication.
 */
@RestController
@RequestMapping("user/books")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "JWT Authentication")
@Tag(name = "User Book Management", description = "Endpoints for borrowing and returning books by authenticated users")
public class UserBookController {
    private final IBookService bookService; // Service for book borrowing and returning operations

    /**
     * Allows the authenticated user to borrow a book by its ID.
     *
     * @param bookId the ID of the book to borrow
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a {@link BorrowingResponse} detailing the borrowing
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - book not found (ErrorCode.BOOK_NOT_EXISTED)
     *                      - book already borrowed (ErrorCode.BOOK_ALREADY_BORROWED)
     * @implNote Delegates borrowing to {@link IBookService} and returns the borrowing details in an {@link ApiResponse}.
     */
    @PostMapping("/borrow/{bookId}")
    @Operation(summary = "Borrow a book",
            description = "Allows the authenticated user to borrow a book by its ID.")
    public ResponseEntity<ApiResponse<BorrowingResponse>> borrowBooks(@PathVariable Long bookId) {
        ApiResponse<BorrowingResponse> response = ApiResponse.<BorrowingResponse>builder()
                .message("Borrow book is successfully!")
                .result(bookService.borrowBook(bookId))
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Allows the authenticated user to return a borrowed book by its ID.
     *
     * @param bookId the ID of the book to return
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a {@link BorrowingResponse} detailing the return
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - book not found (ErrorCode.BOOK_NOT_EXISTED)
     *                      - book not borrowed by user (ErrorCode.BOOK_NOT_BORROWED)
     * @implNote Delegates returning to {@link IBookService} and returns the updated borrowing details in an {@link ApiResponse}.
     */
    @PostMapping("/return/{bookId}")
    @Operation(summary = "Return a book",
            description = "Allows the authenticated user to return a borrowed book by its ID.")
    public ResponseEntity<ApiResponse<BorrowingResponse>> returnBooks(@PathVariable Long bookId) {
        ApiResponse<BorrowingResponse> response = ApiResponse.<BorrowingResponse>builder()
                .message("Return book is successfully!")
                .result(bookService.returnBook(bookId))
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a paginated list of books currently borrowed by the authenticated user.
     *
     * @param offset the page number (starting from 0)
     * @param limit  the number of items per page
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a {@link Page} of {@link BorrowingResponse} objects
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - invalid pagination parameters (ErrorCode.INVALID_INPUT)
     * @implNote Uses {@link Pageable} to fetch borrowed books from {@link IBookService} and wraps them in an {@link ApiResponse}.
     */
    @GetMapping("/books-borrow")
    @Operation(summary = "Get borrowed books",
            description = "Retrieves a paginated list of books currently borrowed by the authenticated user.")
    @Parameter(name = "offset", description = "Page number (default: 0)")
    @Parameter(name = "limit", description = "Items per page (default: 10)")
    public ResponseEntity<ApiResponse<Page<BorrowingResponse>>> getBookBorrow(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        ApiResponse<Page<BorrowingResponse>> response = ApiResponse.<Page<BorrowingResponse>>builder()
                .message("List of borrowed books.")
                .result(bookService.getBookBorrowForUser(pageable))
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a paginated list of books previously returned by the authenticated user.
     *
     * @param offset the page number (starting from 0)
     * @param limit  the number of items per page
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a {@link Page} of {@link BorrowingResponse} objects
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - invalid pagination parameters (ErrorCode.INVALID_INPUT)
     * @implNote Uses {@link Pageable} to fetch returned books from {@link IBookService} and wraps them in an {@link ApiResponse}.
     */
    @GetMapping("/books-return")
    @Operation(summary = "Get returned books",
            description = "Retrieves a paginated list of books previously returned by the authenticated user.")
    @Parameter(name = "offset", description = "Page number (default: 0)")
    @Parameter(name = "limit", description = "Items per page (default: 10)")
    public ResponseEntity<ApiResponse<Page<BorrowingResponse>>> getBookReturn(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        ApiResponse<Page<BorrowingResponse>> response = ApiResponse.<Page<BorrowingResponse>>builder()
                .message("List of borrowed books.")
                .result(bookService.getBookReturnForUser(pageable))
                .build();
        return ResponseEntity.ok(response);
    }
}