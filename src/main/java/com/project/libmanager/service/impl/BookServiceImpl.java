package com.project.libmanager.service.impl;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.constant.UserAction;
import com.project.libmanager.criteria.BookCriteria;
import com.project.libmanager.entity.Book;
import com.project.libmanager.entity.BookType;
import com.project.libmanager.entity.Borrowing;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.repository.BookRepository;
import com.project.libmanager.repository.BookTypeRepository;
import com.project.libmanager.repository.BorrowingRepository;
import com.project.libmanager.repository.UserRepository;
import com.project.libmanager.service.IActivityLogService;
import com.project.libmanager.service.IBookService;
import com.project.libmanager.service.dto.request.BookCreateRequest;
import com.project.libmanager.service.dto.request.BookUpdateRequest;
import com.project.libmanager.service.dto.response.BookResponse;
import com.project.libmanager.service.dto.response.BorrowingResponse;
import com.project.libmanager.service.mapper.BookMapper;
import com.project.libmanager.service.mapper.BookTypeMapper;
import com.project.libmanager.service.mapper.BorrowingMapper;
import com.project.libmanager.specification.BookQueryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@link IBookService} for managing book-related operations in a library system.
 * Provides functionality for creating, updating, deleting, borrowing, returning, and searching books.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BookServiceImpl implements IBookService {
    private final BookRepository bookRepository;         // Manages book persistence and queries
    private final BookTypeRepository bookTypeRepository; // Handles book type lookups
    private final UserRepository userRepository;         // Provides user data access
    private final BookMapper bookMapper;                 // Converts between Book entities and DTOs
    private final BookTypeMapper bookTypeMapper;         // Converts between BookType entities and DTOs
    private final BorrowingRepository borrowingRepository; // Manages borrowing records
    private final BorrowingMapper borrowingMapper;       // Converts between Borrowing entities and DTOs
    private final BookQueryService bookQueryService;     // Executes complex book queries with criteria
    private final IActivityLogService activityLogService; // Logs user and admin actions for auditing

    /**
     * Creates a new book or updates an existing book if the ISBN already exists.
     *
     * @param bookCreateRequest the {@link BookCreateRequest} containing:
     *                          - isbn: unique ISBN identifier (required)
     *                          - typeId: book type ID (required)
     *                          - stock: number of copies to add (required)
     *                          - other book metadata (e.g., title, author)
     * @return a {@link BookResponse} with details of the created or updated book
     * @throws AppException if:
     *                      - request is null (ErrorCode.UNCATEGORIZED_EXCEPTION)
     *                      - book type not found (ErrorCode.BOOKTYPE_NOT_EXISTED)
     *                      - database error occurs (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Updates stock if ISBN exists; otherwise, creates a new book and logs the action.
     */
    @Transactional
    @Override
    public BookResponse createBook(BookCreateRequest bookCreateRequest) {
        // Validate input; null check ensures robustness
        if (bookCreateRequest == null) {
            log.error("BookCreateRequest is null");
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // Fetch book type; fails fast if invalid ID
        BookType type = bookTypeRepository.findById(bookCreateRequest.getTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKTYPE_NOT_EXISTED));

        try {
            // Check for existing book by ISBN; assumes ISBN uniqueness
            Optional<Book> existingBook = bookRepository.findByIsbn(bookCreateRequest.getIsbn());
            // if existing book with ISBN
            if (existingBook.isPresent()) {
                // Update existing book's stock; preserves other fields
                Book book = existingBook.get();
                book.setStock(book.getStock() + bookCreateRequest.getStock());
                book = bookRepository.save(book);
                return bookMapper.toBookResponse(book); // Return updated book details
            }
            // Create new book; maps request to entity
            Book book = bookMapper.toBook(bookCreateRequest);
            book.setType(type); // Link to book type
            book = bookRepository.save(book); // Persist new book

            // Log admin action; captures creation event
            User user = getAuthenticatedUser();
            BookResponse bookResponse = bookMapper.toBookResponse(book);
            activityLogService.logAction(
                    user.getId(),
                    user.getEmail(),
                    UserAction.ADD_BOOK,
                    "Admin add new book with id: " + book.getId(),
                    bookResponse,
                    null
            );
            return bookResponse;
        } catch (DataAccessException e) {
            log.error("Database error: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Updates an existing book's information.
     *
     * @param bookUpdateRequest the {@link BookUpdateRequest} with updated book details
     * @param bookId            the ID of the book to update
     * @return a {@link BookResponse} with the updated book details
     * @throws AppException if:
     *                      - request is null (ErrorCode.INVALID_KEY)
     *                      - book not found (ErrorCode.BOOK_NOT_EXISTED)
     *                      - book type not found (ErrorCode.BOOKTYPE_NOT_EXISTED)
     *                      - ISBN conflicts with another book (ErrorCode.BOOK_EXISTED)
     *                      - database error occurs (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Ensures ISBN uniqueness (except for the same book) and logs old/new states.
     */
    @Transactional
    @Override
    public BookResponse updateBook(BookUpdateRequest bookUpdateRequest, Long bookId) {
        // Validate request; null check for safety
        if (bookUpdateRequest == null) {
            log.error("BookCreateRequest is null"); // Typo: should be BookUpdateRequest
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        // Fetch existing book; fails if not found
        Book oldBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));
        BookResponse oldBookResponse = bookMapper.toBookResponse(oldBook);

        // Fetch book type; ensures valid type ID
        BookType type = bookTypeRepository.findById(bookUpdateRequest.getTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKTYPE_NOT_EXISTED));
        // Check ISBN uniqueness; allows same ISBN for this book only
        if (!oldBook.getIsbn().equals(bookUpdateRequest.getIsbn())
                && bookRepository.findByIsbn(bookUpdateRequest.getIsbn()).isPresent()) {
            throw new AppException(ErrorCode.BOOK_EXISTED);
        }

        try {
            // Update book fields; assumes mapper merges correctly
            bookMapper.updateBook(oldBook, bookUpdateRequest);
            oldBook.setType(type); // Update type association
            Book newBook = bookRepository.save(oldBook); // Save changes

            // Log admin action with before/after states
            User user = getAuthenticatedUser();
            BookResponse newBookResponse = bookMapper.toBookResponse(newBook);
            activityLogService.logAction(
                    user.getId(),
                    user.getEmail(),
                    UserAction.UPDATE_BOOK_INFO,
                    "Admin update book with id: " + newBook.getId(),
                    oldBookResponse,
                    newBookResponse
            );
            return newBookResponse;
        } catch (DataAccessException e) {
            // Log database error
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Deletes a book by marking it as deleted.
     *
     * @param id the ID of the book to delete
     * @throws AppException if:
     *                      - book not found (ErrorCode.BOOK_NOT_EXISTED)
     *                      - book is currently borrowed (ErrorCode.BOOK_IS_BORROW)
     *                      - error occurs (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Uses soft deletion and logs the action; prevents deletion if book is borrowed.
     */
    @Transactional
    @Override
    public void deleteBook(Long id) {
        // Fetch book; fails if not found
        Book book = bookRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));
        BookResponse oleBookResponse = bookMapper.toBookResponse(book); // Typo: should be oldBookResponse
        // Check borrowing status; null return date indicates active borrowing
        boolean isBorrowed = borrowingRepository.existsByBookAndReturnDateIsNull(book);
        if (isBorrowed) {
            throw new AppException(ErrorCode.BOOK_IS_BORROW);
        }
        try {
            // Soft delete; marks as deleted instead of removing
            book.setDeleted(true);
            bookRepository.save(book);

            // Log admin action
            User user = getAuthenticatedUser();
            activityLogService.logAction(
                    user.getId(),
                    user.getEmail(),
                    UserAction.DELETE_BOOK,
                    "Admin deleted book with id: " + book.getId(),
                    oleBookResponse,
                    null
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Retrieves a paginated list of available books.
     *
     * @param pageable the {@link Pageable} object with pagination details
     * @return a {@link Page} of {@link BookResponse} containing available books
     * @throws AppException if no books are found (ErrorCode.BOOK_NOT_EXISTED)
     * @implNote Fetches only non-deleted books and maps them to responses.
     */
    @Override
    public Page<BookResponse> getBooks(Pageable pageable) {
        // Fetch available books; assumes custom query excludes deleted books
        Page<Book> pageBook = bookRepository.findAllAvailableBooks(pageable);
        if (pageBook.isEmpty()) {
            log.error("Book not found in the database");
            throw new AppException(ErrorCode.BOOK_NOT_EXISTED);
        }
        return mapBookPageBookResponsePage(pageBook);
    }

    /**
     * Retrieves a paginated list of all books (including deleted) for admin use.
     *
     * @param pageable the {@link Pageable} object with pagination details
     * @return a {@link Page} of {@link BookResponse} containing all books
     * @throws AppException if no books are found (ErrorCode.BOOK_NOT_EXISTED)
     * @implNote Fetches all books regardless of deletion status for admin visibility.
     */
    @Override
    public Page<BookResponse> getBooksForAdmin(Pageable pageable) {
        // Fetch all books; includes deleted ones
        Page<Book> pageBook = bookRepository.findAll(pageable);
        if (pageBook.isEmpty()) {
            log.error("Book not found in the database");
            throw new AppException(ErrorCode.BOOK_NOT_EXISTED);
        }
        return mapBookPageBookResponsePage(pageBook);
    }

    /**
     * Maps a page of books to a page of book responses.
     *
     * @param bookPage the {@link Page} of {@link Book} entities
     * @return a {@link Page} of {@link BookResponse} with mapped data
     * @implNote Converts book entities to DTOs while preserving pagination metadata.
     */
    private Page<BookResponse> mapBookPageBookResponsePage(Page<Book> bookPage) {
        // Map each book to response DTO; includes type details
        List<BookResponse> bookResponses = bookPage.getContent().stream()
                .map(book -> mapToBookResponseByMapper(book.getId()))
                .toList();
        // Construct paginated response; maintains original page structure
        return new PageImpl<>(bookResponses, bookPage.getPageable(), bookPage.getTotalElements());
    }

    /**
     * Maps a book to its response DTO by ID.
     *
     * @param id the ID of the book
     * @return a {@link BookResponse} with book and type details
     * @throws AppException if book not found (ErrorCode.BOOK_NOT_EXISTED)
     * @implNote Enriches book response with book type information.
     */
    private BookResponse mapToBookResponseByMapper(Long id) {
        // Fetch book; fails if not found
        Book book = bookRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));
        BookResponse bookResponse = bookMapper.toBookResponse(book);
        // Add book type details; assumes type is always present
        bookResponse.setBookType(bookTypeMapper.toBookTypeResponse(book.getType()));
        return bookResponse;
    }

    /**
     * Retrieves details of a specific book for regular users.
     *
     * @param id the ID of the book
     * @return a {@link BookResponse} with book details
     * @throws AppException if:
     *                      - book not found (ErrorCode.BOOK_NOT_EXISTED)
     *                      - book is deleted (ErrorCode.BOOK_IS_DELETED)
     * @implNote Restricts access to deleted books for non-admin users.
     */
    @Override
    public BookResponse getBook(Long id) {
        // Fetch book; fails if not found
        Book book = bookRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));
        // Check deletion status; hides deleted books from regular users
        if (book.isDeleted()) {
            throw new AppException(ErrorCode.BOOK_IS_DELETED);
        }
        return mapToBookResponseByMapper(id);
    }

    /**
     * Retrieves details of a specific book for admin use (includes deleted books).
     *
     * @param id the ID of the book
     * @return a {@link BookResponse} with book details
     * @throws AppException if book not found (ErrorCode.BOOK_NOT_EXISTED)
     * @implNote Allows admins to view all books, including deleted ones.
     */
    @Override
    public BookResponse getBookForAdmin(Long id) {
        return mapToBookResponseByMapper(id);
    }

    /**
     * Retrieves the authenticated user from the security context.
     *
     * @return the {@link User} entity of the authenticated user
     * @throws AppException if:
     *                      - authentication is missing (ErrorCode.UNAUTHORIZED)
     *                      - user not found (ErrorCode.USER_NOT_EXISTED)
     * @implNote Uses JWT principal (email) to fetch user; assumes email uniqueness.
     */
    private User getAuthenticatedUser() {
        // Access security context; assumes JWT-based authentication
        SecurityContext jwtContext = SecurityContextHolder.getContext();
        // Validate authentication; fails fast if invalid
        if (jwtContext == null || jwtContext.getAuthentication() == null ||
                !jwtContext.getAuthentication().isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        // Log principal for debugging; assumes email as subject
        log.info("Authentication {}", jwtContext.getAuthentication().getName());

        // Fetch user by email; assumes reliable identifier
        String email = jwtContext.getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    /**
     * Allows an authenticated user to borrow a book.
     *
     * @param bookId the ID of the book to borrow
     * @return a {@link BorrowingResponse} with borrowing details
     * @throws AppException if:
     *                      - user is deleted (ErrorCode.USER_IS_DELETED)
     *                      - user is banned (ErrorCode.USER_BORROWING_RESTRICTED)
     *                      - user has overdue books (ErrorCode.USER_HAS_OVERDUE_BOOKS)
     *                      - book not found (ErrorCode.BOOK_NOT_EXISTED)
     *                      - book out of stock (ErrorCode.BOOK_OUT_OF_STOCK)
     *                      - book already borrowed (ErrorCode.BOOK_ALREADY_BORROWED)
     *                      - error occurs (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Validates user/book status, creates borrowing record, and decrements stock.
     */
    @Transactional
    @Override
    public BorrowingResponse borrowBook(Long bookId) {
        // Fetch authenticated user; ensures valid session
        User user = getAuthenticatedUser();
        // Check user status; restricts deleted users
        boolean isDeleted = user.isDeleted();
        if (isDeleted) {
            throw new AppException(ErrorCode.USER_IS_DELETED);
        }
        // Check borrowing ban; assumes ban logic elsewhere
        if (user.isBannedFromBorrowing()) {
            throw new AppException(ErrorCode.USER_BORROWING_RESTRICTED);
        }
        // Check overdue books; prevents borrowing if overdue
        if (borrowingRepository.existsOverdueBorrowingsByUser(user.getId())) {
            throw new AppException(ErrorCode.USER_HAS_OVERDUE_BOOKS);
        }

        // Fetch book; fails if not found
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));
        // Check stock availability; ensures book is borrowable
        if (book.getStock() < 1) {
            throw new AppException(ErrorCode.BOOK_OUT_OF_STOCK);
        }
        // Check if user already borrowed this book; prevents duplicates
        if (borrowingRepository.existsByUserIdAndBookIdAndReturnDateIsNull(user.getId(), bookId)) {
            throw new AppException(ErrorCode.BOOK_ALREADY_BORROWED);
        }

        try {
            // Calculate borrow and due dates; uses book's max borrow period
            Instant borrowDate = Instant.now();
            Instant dueDate = borrowDate.plus(book.getMaxBorrowDays(), ChronoUnit.DAYS);

            // Build and save borrowing record
            Borrowing borrowing = Borrowing.builder()
                    .user(user)
                    .book(book)
                    .borrowDate(borrowDate)
                    .dueDate(dueDate)
                    .build();
            borrowingRepository.save(borrowing);

            // Update stock; reflects borrowing
            book.setStock(book.getStock() - 1);
            bookRepository.save(book);

            // Log user action
            activityLogService.logAction(
                    user.getId(),
                    user.getEmail(),
                    UserAction.BOOK_BORROWED,
                    "User borrowed book with id: " + book.getId(),
                    null,
                    null
            );
            return borrowingMapper.toBorrowingResponse(borrowing);
        } catch (Exception e) {
            // Log error for debugging
            log.error("Error borrowing book: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Allows an authenticated user to return a borrowed book.
     *
     * @param bookId the ID of the book to return
     * @return a {@link BorrowingResponse} with return details
     * @throws AppException if:
     *                      - book not borrowed by user (ErrorCode.BOOK_NOT_BORROWED)
     *                      - error occurs (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Updates return date, increments stock, and tracks late returns.
     */
    @Transactional
    @Override
    public BorrowingResponse returnBook(Long bookId) {
        // Fetch authenticated user
        User user = getAuthenticatedUser();

        // Fetch active borrowing; fails if not found
        Borrowing borrowing = borrowingRepository.findByUserIdAndBookIdAndReturnDateIsNull(user.getId(), bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_BORROWED));

        // Set return date; marks borrowing as completed
        Instant returnDate = Instant.now();
        borrowing.setReturnDate(returnDate);

        // Track late returns; increments counter if overdue
        if (returnDate.isAfter(borrowing.getDueDate())) {
            user.setLateReturnCount(user.getLateReturnCount() + 1);
        }

        try {
            // Save user updates; persists late return count
            userRepository.save(user);

            // Update stock; reflects return
            Book book = borrowing.getBook();
            book.setStock(book.getStock() + 1);
            bookRepository.save(book);

            // Log user action
            activityLogService.logAction(
                    user.getId(),
                    user.getEmail(),
                    UserAction.BOOK_RETURNED,
                    "User returned book with id: " + book.getId(),
                    null,
                    null
            );
            return borrowingMapper.toBorrowingResponse(borrowingRepository.save(borrowing));
        } catch (Exception e) {
            // Log error
            log.error("Error returning book: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Retrieves a paginated list of books currently borrowed by a specific user.
     *
     * @param userId   the ID of the user
     * @param pageable the {@link Pageable} object with pagination details
     * @return a {@link Page} of {@link BorrowingResponse} with borrowed books
     * @throws AppException if error occurs (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Fetches active borrowings (null return date) and maps to responses.
     */
    @Override
    public Page<BorrowingResponse> getBookBorrowByUser(Long userId, Pageable pageable) {
        try {
            // Fetch active borrowings for user
            Page<Borrowing> borrowings = borrowingRepository.findByUserIdAndReturnDateIsNull(userId, pageable);
            return mapBorrowPageBrorrowResponsePage(borrowings);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Retrieves a paginated list of books currently borrowed by the authenticated user.
     *
     * @param pageable the {@link Pageable} object with pagination details
     * @return a {@link Page} of {@link BorrowingResponse} with borrowed books
     * @throws AppException if error occurs (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Fetches active borrowings for the current user and maps to responses.
     */
    @Override
    public Page<BorrowingResponse> getBookBorrowForUser(Pageable pageable) {
        try {
            User user = getAuthenticatedUser();
            Page<Borrowing> borrowingsBook = borrowingRepository.findByUserIdAndReturnDateIsNull(user.getId(), pageable);
            return mapBorrowPageBrorrowResponsePage(borrowingsBook);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Retrieves a paginated list of books returned by the authenticated user.
     *
     * @param pageable the {@link Pageable} object with pagination details
     * @return a {@link Page} of {@link BorrowingResponse} with returned books
     * @throws AppException if error occurs (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Fetches completed borrowings (non-null return date) for the current user.
     */
    @Override
    public Page<BorrowingResponse> getBookReturnForUser(Pageable pageable) {
        try {
            User user = getAuthenticatedUser();
            Page<Borrowing> bookReturn = borrowingRepository.findByUserIdAndReturnDateIsNotNull(user.getId(), pageable);
            return mapBorrowPageBrorrowResponsePage(bookReturn);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Maps a page of borrowings to a page of borrowing responses.
     *
     * @param borrowingPage the {@link Page} of {@link Borrowing} entities
     * @return a {@link Page} of {@link BorrowingResponse} with mapped data
     * @implNote Converts borrowing entities to DTOs while preserving pagination metadata.
     */
    private Page<BorrowingResponse> mapBorrowPageBrorrowResponsePage(Page<Borrowing> borrowingPage) {
        // Map each borrowing to response DTO
        List<BorrowingResponse> borrowingResponse = borrowingPage.getContent().stream()
                .map(borrowing -> mapToBorrowResponseByMapper(borrowing.getId()))
                .toList();
        // Construct paginated response
        return new PageImpl<>(borrowingResponse, borrowingPage.getPageable(), borrowingPage.getTotalElements());
    }

    /**
     * Maps a borrowing to its response DTO by ID.
     *
     * @param id the ID of the borrowing
     * @return a {@link BorrowingResponse} with borrowing details
     * @throws AppException if borrowing not found (ErrorCode.BORROW_NOT_FOUND)
     * @implNote Fetches borrowing and converts to response DTO.
     */
    private BorrowingResponse mapToBorrowResponseByMapper(Long id) {
        // Fetch borrowing; fails if not found
        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BORROW_NOT_FOUND));
        return borrowingMapper.toBorrowingResponse(borrowing);
    }

    /**
     * Imports books from a CSV file, updating existing books or creating new ones.
     *
     * @param file the {@link MultipartFile} CSV file with book data
     * @throws AppException if:
     *                      - file is empty (ErrorCode.FILE_EMPTY)
     *                      - file exceeds size limit (ErrorCode.FILE_LIMIT)
     *                      - book type not found (ErrorCode.BOOKTYPE_NOT_EXISTED)
     *                      - error occurs (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Parses CSV, updates stock for existing ISBNs, creates new books, and logs action.
     */
    @Transactional
    @Override
    public void importBooks(MultipartFile file) {
        // Validate file; ensures non-empty input
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }
        // Check file size; enforces 5KB limit (5 * 1024 bytes)
        if (file.getSize() > 5 * 1024) {
            throw new AppException(ErrorCode.FILE_LIMIT);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            // Parse CSV; expects headers and skips them, trims spaces
            CSVParser csvParser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreSurroundingSpaces(true)
                    .build()
                    .parse(reader);

            // Define date formatter; assumes "yyyy/MM/d" format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/d");

            // Track books to update; avoids redundant saves
            Map<String, Book> booksToUpdate = new HashMap<>();
            // Process CSV rows; maps to Book entities
            List<Book> newBooks = csvParser.getRecords().stream().map(csvRow -> {
                String isbn = csvRow.get("isbn");
                LocalDate localDate = LocalDate.parse(csvRow.get("publishedDate"), formatter);
                return bookRepository.findByIsbn(isbn)
                        .map(book -> {
                            // Update stock for existing book
                            book.setStock(book.getStock() + Integer.parseInt(csvRow.get("stock")));
                            booksToUpdate.put(isbn, book);
                            return book;
                        })
                        .orElseGet(() -> Book.builder()
                                // Create new book with all fields from CSV
                                .isbn(isbn)
                                .title(csvRow.get("title"))
                                .author(csvRow.get("author"))
                                .type(bookTypeRepository.findById(Long.parseLong(csvRow.get("typeId")))
                                        .orElseThrow(() -> new AppException(ErrorCode.BOOKTYPE_NOT_EXISTED)))
                                .stock(Integer.parseInt(csvRow.get("stock")))
                                .publisher(csvRow.get("publisher"))
                                .publishedDate(localDate.atStartOfDay(ZoneId.of("UTC")).toInstant())
                                .maxBorrowDays(Integer.parseInt(csvRow.get("maxBorrowDays")))
                                .location(csvRow.get("location"))
                                .coverImageUrl(csvRow.get("coverImageUrl"))
                                .deleted(false)
                                .build());
            }).toList();

            // Save updates and new books; transactional ensures atomicity
            bookRepository.saveAll(booksToUpdate.values());
            bookRepository.saveAll(newBooks);

            // Log admin action
            User user = getAuthenticatedUser();
            activityLogService.logAction(
                    user.getId(),
                    user.getEmail(),
                    UserAction.IMPORT_BOOK_BY_CSV,
                    "Admin import book by file csv success!",
                    null,
                    null
            );
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Searches books based on specified criteria.
     *
     * @param criteria the {@link BookCriteria} with search parameters (e.g., title, author)
     * @param pageable the {@link Pageable} object with pagination details
     * @return a {@link Page} of {@link BookResponse} with matching books
     * @implNote Delegates to BookQueryService for filtering and maps results to responses.
     */
    @Override
    public Page<BookResponse> searchBook(BookCriteria criteria, Pageable pageable) {
        // Fetch books by criteria; assumes BookQueryService handles filtering
        Page<Book> books = bookQueryService.findByCriteria(criteria, pageable);
        return mapBookPageBookResponsePage(books);
    }
}