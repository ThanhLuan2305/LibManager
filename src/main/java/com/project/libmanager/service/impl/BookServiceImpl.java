package com.project.libmanager.service.impl;

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

import com.project.libmanager.constant.UserAction;
import com.project.libmanager.criteria.BookCriteria;
import com.project.libmanager.service.IActivityLogService;
import com.project.libmanager.specification.BookQueryService;
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

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.service.dto.request.BookCreateRequest;
import com.project.libmanager.service.dto.request.BookUpdateRequest;
import com.project.libmanager.service.dto.response.BookResponse;
import com.project.libmanager.service.dto.response.BorrowingResponse;
import com.project.libmanager.entity.Book;
import com.project.libmanager.entity.BookType;
import com.project.libmanager.entity.Borrowing;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.service.mapper.BookMapper;
import com.project.libmanager.service.mapper.BookTypeMapper;
import com.project.libmanager.service.mapper.BorrowingMapper;
import com.project.libmanager.repository.BookRepository;
import com.project.libmanager.repository.BookTypeRepository;
import com.project.libmanager.repository.BorrowingRepository;
import com.project.libmanager.repository.UserRepository;
import com.project.libmanager.service.IBookService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookServiceImpl implements IBookService {
    private final BookRepository bookRepository;
    private final BookTypeRepository bookTypeRepository;
    private final UserRepository userRepository;
    private final BookMapper bookMapper;
    private final BookTypeMapper bookTypeMapper;
    private final BorrowingRepository borrowingRepository;
    private final BorrowingMapper borrowingMapper;
    private final BookQueryService bookQueryService;
    private final IActivityLogService activityLogService;

    /**
     * Creates a new book or updates an existing book if the ISBN already exists.
     *
     * @param bookCreateRequest The request containing information about the book to
     *                          be created.
     * @return The response containing the details of the created or updated book.
     * @throws AppException If the book type does not exist or there is a database
     *                      error.
     * @implNote If the book with the same ISBN exists, the stock will be updated.
     *           Otherwise, a new book will be created.
     */
    @Transactional
    @Override
    public BookResponse createBook(BookCreateRequest bookCreateRequest) {
        if (bookCreateRequest == null) {
            log.error("BookCreateRequest is null");
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        BookType type = bookTypeRepository.findById(bookCreateRequest.getTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKTYPE_NOT_EXISTED));

        try {
            Optional<Book> existingBook = bookRepository.findByIsbn(bookCreateRequest.getIsbn());

            if (existingBook.isPresent()) {
                Book book = existingBook.get();
                book.setStock(book.getStock() + bookCreateRequest.getStock());
                book = bookRepository.save(book);
                return bookMapper.toBookResponse(book);
            }
            Book book = bookMapper.toBook(bookCreateRequest);
            book.setType(type);
            book = bookRepository.save(book);
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
     * Updates the information of an existing book.
     *
     * @param bookUpdateRequest The request containing updated information about the
     *                          book.
     * @param bookId            The ID of the book to be updated.
     * @return The response containing the updated book details.
     * @throws AppException If the book does not exist, the book type is invalid, or
     *                      any other error occurs.
     * @implNote This method updates the book if the ISBN is unique; otherwise, it
     *           throws an error if a book with the same ISBN exists.
     */
    @Transactional
    @Override
    public BookResponse updateBook(BookUpdateRequest bookUpdateRequest, Long bookId) {
        if (bookUpdateRequest == null) {
            log.error("BookCreateRequest is null");
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        Book oldBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        BookResponse oldBookResponse = bookMapper.toBookResponse(oldBook);
        BookType type = bookTypeRepository.findById(bookUpdateRequest.getTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKTYPE_NOT_EXISTED));
        if (!oldBook.getIsbn().equals(bookUpdateRequest.getIsbn())
                && bookRepository.findByIsbn(bookUpdateRequest.getIsbn()).isPresent()) {
            throw new AppException(ErrorCode.BOOK_EXISTED);
        }

        try {
            bookMapper.updateBook(oldBook, bookUpdateRequest);
            oldBook.setType(type);
            Book newBook = bookRepository.save(oldBook);

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
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Deletes a book from the system.
     *
     * @param id The ID of the book to be deleted.
     * @throws AppException If the book does not exist or is currently borrowed.
     * @implNote This method checks if the book is borrowed before attempting to
     *           delete it.
     */
    @Transactional
    @Override
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));
        BookResponse oleBookResponse = bookMapper.toBookResponse(book);
        boolean isBorrowed = borrowingRepository.existsByBookAndReturnDateIsNull(book);
        if (isBorrowed) {
            throw new AppException(ErrorCode.BOOK_IS_BORROW);
        }
        try {
            book.setDeleted(true);
            bookRepository.save(book);

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
     * Fetches a paginated list of books.
     *
     * @param pageable Pagination details.
     * @return A page of books.
     * @throws AppException If there is an error while fetching the books.
     * @implNote This method retrieves books from the database and returns them in a
     *           paginated format.
     */
    @Override
    public Page<BookResponse> getBooks(Pageable pageable) {
        Page<Book> pageBook = bookRepository.findAllAvailableBooks(pageable);
        if (pageBook.isEmpty()) {
            log.error("Book not found in the database");
            throw new AppException(ErrorCode.BOOK_NOT_EXISTED);
        }
        return mapBookPageBookResponsePage(pageBook);
    }

    @Override
    public Page<BookResponse> getBooksForAdmin(Pageable pageable) {
        Page<Book> pageBook = bookRepository.findAll(pageable);
        if (pageBook.isEmpty()) {
            log.error("Book not found in the database");
            throw new AppException(ErrorCode.BOOK_NOT_EXISTED);
        }
        return mapBookPageBookResponsePage(pageBook);
    }

    /**
     * Converts a page of books to a page of book responses.
     *
     * @param bookPage The page of books.
     * @return A page of book responses.
     * @implNote This method maps the content of the book page to a list of book
     *           responses and returns a paginated response.
     */
    private Page<BookResponse> mapBookPageBookResponsePage(Page<Book> bookPage) {
        List<BookResponse> bookResponses = bookPage.getContent().stream()
                .map(book -> mapToBookResponseByMapper(book.getId()))
                .toList();

        return new PageImpl<>(bookResponses, bookPage.getPageable(), bookPage.getTotalElements());
    }

    /**
     * Fetches the response of a book by its ID.
     *
     * @param id The ID of the book.
     * @return The response of the book.
     * @throws AppException If the book does not exist.
     * @implNote This method retrieves a single book from the repository and returns
     *           its response.
     */
    private BookResponse mapToBookResponseByMapper(Long id) {

        Book book = bookRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));
        BookResponse bookResponse = bookMapper.toBookResponse(book);
        bookResponse.setBookType(bookTypeMapper.toBookTypeResponse(book.getType()));
        return bookResponse;
    }

    /**
     * Fetches the details of a specific book.
     *
     * @param id The ID of the book.
     * @return The response containing the details of the book.
     * @throws AppException If the book does not exist.
     * @implNote This method fetches a specific book and returns its response.
     */
    @Override
    public BookResponse getBook(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));
        if (book.isDeleted()) {
            throw  new AppException(ErrorCode.BOOK_IS_DELETED);
        }
        return mapToBookResponseByMapper(id);
    }

    @Override
    public BookResponse getBookForAdmin(Long id) {
        return mapToBookResponseByMapper(id);
    }

    private User getAuthenticatedUser() {
        SecurityContext jwtContext = SecurityContextHolder.getContext();
        if (jwtContext == null || jwtContext.getAuthentication() == null ||
                !jwtContext.getAuthentication().isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        log.info("Authentication {}", jwtContext.getAuthentication().getName());

        String email = jwtContext.getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    /**
     * Allows an authenticated user to borrow a book.
     *
     * @param bookId The ID of the book to be borrowed.
     * @return A BorrowingResponse containing details of the borrowed book.
     * @throws AppException If the user is deleted, has overdue books, the book is out of stock,
     *                      or the book has already been borrowed.
     * @implNote This method checks user status, verifies book availability, and records the borrowing transaction.
     */
    @Override
    @Transactional
    public BorrowingResponse borrowBook(Long bookId) {
        User user = getAuthenticatedUser();
        boolean isDeleted = user.isDeleted();
        if (isDeleted) {
            throw new AppException(ErrorCode.USER_IS_DELETED);
        }
        if(user.isBannedFromBorrowing()) {
            throw new AppException(ErrorCode.USER_BORROWING_RESTRICTED);
        }

        if (borrowingRepository.existsOverdueBorrowingsByUser(user.getId())) {
            throw new AppException(ErrorCode.USER_HAS_OVERDUE_BOOKS);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        if (book.getStock() < 1) {
            throw new AppException(ErrorCode.BOOK_OUT_OF_STOCK);
        }

        if (borrowingRepository.existsByUserIdAndBookIdAndReturnDateIsNull(user.getId(), bookId)) {
            throw new AppException(ErrorCode.BOOK_ALREADY_BORROWED);
        }

        try {
            Instant borrowDate = Instant.now();
            Instant dueDate = borrowDate.plus(book.getMaxBorrowDays(), ChronoUnit.DAYS);

            Borrowing borrowing = Borrowing.builder()
                    .user(user)
                    .book(book)
                    .borrowDate(borrowDate)
                    .dueDate(dueDate)
                    .build();

            borrowingRepository.save(borrowing);

            book.setStock(book.getStock() - 1);
            bookRepository.save(book);

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
            log.error("Error borrowing book: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Allows an authenticated user to return a borrowed book.
     *
     * @param bookId The ID of the book to be returned.
     * @return A BorrowingResponse containing details of the returned book.
     * @throws AppException If the book was not borrowed by the user.
     * @implNote This method verifies the borrowing record, updates the return date,
     *           and adjusts the book stock accordingly.
     */
    @Override
    @Transactional
    public BorrowingResponse returnBook(Long bookId) {
        User user = getAuthenticatedUser();

        Borrowing borrowing = borrowingRepository.findByUserIdAndBookIdAndReturnDateIsNull(user.getId(), bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_BORROWED));

        Instant returnDate = Instant.now();
        borrowing.setReturnDate(returnDate);

        if (returnDate.isAfter(borrowing.getDueDate())) {
            user.setLateReturnCount(user.getLateReturnCount() + 1);
        }

        try {
            userRepository.save(user);

            Book book = borrowing.getBook();
            book.setStock(book.getStock() + 1);
            bookRepository.save(book);

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
            log.error("Error returning book: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Fetches a paginated list of books borrowed by a user.
     *
     * @param userId   The ID of the user.
     * @param pageable Pagination details.
     * @return A paginated list of borrowed books.
     * @throws AppException If the user has no borrowed books or there is an error.
     * @implNote This method retrieves books borrowed by the user and returns them
     *           in a paginated format.
     */
    @Override
    public Page<BorrowingResponse> getBookBorrowByUser(Long userId, Pageable pageable) {
        try {
            Page<Borrowing> borrowings = borrowingRepository.findByUserIdAndReturnDateIsNull(userId, pageable);

            return mapBorrowPageBrorrowResponsePage(borrowings);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Retrieves a paginated list of books currently borrowed by the authenticated
     * user.
     *
     * @param pageable Pagination details.
     * @return A paginated list of books borrowed by the user.
     * @throws AppException If an error occurs while fetching borrowed books.
     * @implNote This method fetches all active borrowings for the authenticated
     *           user,
     *           maps them to book responses, and returns them in a paginated
     *           format.
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

    private Page<BorrowingResponse> mapBorrowPageBrorrowResponsePage(Page<Borrowing> borrowingPage) {
        List<BorrowingResponse> borrowingResponse = borrowingPage.getContent().stream()
                .map(borrowing -> mapToBorrowResponseByMapper(borrowing.getId()))
                .toList();

        return new PageImpl<>(borrowingResponse, borrowingPage.getPageable(), borrowingPage.getTotalElements());
    }


    private BorrowingResponse mapToBorrowResponseByMapper(Long id) {

        Borrowing borrowing = borrowingRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BORROW_NOT_FOUND));

        return borrowingMapper.toBorrowingResponse(borrowing);
    }

    /**
     * Imports books from a CSV file into the system.
     *
     * @param file The CSV file containing book data.
     * @throws RuntimeException If the file is empty, too large, or if an error
     *                          occurs during the import.
     * @implNote This method reads a CSV file, parses it, and either updates the
     *           stock of existing books or creates new books.
     */
    @Override
    @Transactional
    public void importBooks(MultipartFile file) {
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }
        if (file.getSize() > 5 * 1024) {
            throw new AppException(ErrorCode.FILE_LIMIT);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVParser csvParser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreSurroundingSpaces(true)
                    .build()
                    .parse(reader);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/d");

            Map<String, Book> booksToUpdate = new HashMap<>();
            List<Book> newBooks = csvParser.getRecords().stream().map(csvRow -> {
                String isbn = csvRow.get("isbn");
                LocalDate localDate = LocalDate.parse(csvRow.get("publishedDate"), formatter);
                return bookRepository.findByIsbn(isbn)
                        .map(book -> {
                            book.setStock(book.getStock() + Integer.parseInt(csvRow.get("stock")));
                            booksToUpdate.put(isbn, book);
                            return book;
                        })
                        .orElseGet(() -> Book.builder()
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

            bookRepository.saveAll(booksToUpdate.values());
            bookRepository.saveAll(newBooks);

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

    @Override
    public Page<BookResponse> searchBook(BookCriteria criteria, Pageable pageable) {
        Page<Book> books = bookQueryService.findByCriteria(criteria, pageable);
        return mapBookPageBookResponsePage(books);
    }
}
