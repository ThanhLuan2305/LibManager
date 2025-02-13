package com.project.LibManager.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.project.LibManager.dto.request.BookCreateRequest;
import com.project.LibManager.dto.request.BorrowingRequest;
import com.project.LibManager.dto.request.SearchBookRequest;
import com.project.LibManager.dto.response.BookResponse;
import com.project.LibManager.dto.response.BorrowingResponse;
import com.project.LibManager.entity.Book;
import com.project.LibManager.entity.BookType;
import com.project.LibManager.entity.Borrowing;
import com.project.LibManager.entity.User;
import com.project.LibManager.exception.AppException;
import com.project.LibManager.exception.ErrorCode;
import com.project.LibManager.mapper.BookMapper;
import com.project.LibManager.mapper.BookTypeMapper;
import com.project.LibManager.mapper.BorrowwingMapper;
import com.project.LibManager.repository.BookRepository;
import com.project.LibManager.repository.BookTypeRepository;
import com.project.LibManager.repository.BorrowingRepository;
import com.project.LibManager.repository.UserRepository;
import com.project.LibManager.specification.BookSpecification;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BookService {
    BookRepository bookRepository;
    BookTypeRepository bookTypeRepository;
    UserRepository userRepository;
    BookMapper bookMapper;
    BookTypeMapper bookTypeMapper;
    BorrowingRepository borrowingRepository;
    BorrowwingMapper borrowwingMapper;

    @Transactional
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
                bookRepository.save(book);
                return bookMapper.toBookResponse(book);
            } else {
                Book book = bookMapper.toBook(bookCreateRequest);
                book.setType(type);
                bookRepository.save(book);
                return bookMapper.toBookResponse(book);
            }
        } catch (DataAccessException e) {
            log.error("Database error: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }


    @Transactional
    public BookResponse updateBook(BookCreateRequest bookCreateRequest, Long bookId) {
        if (bookCreateRequest == null) {
            log.error("BookCreateRequest is null");
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        BookType type = bookTypeRepository.findById(bookCreateRequest.getTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKTYPE_NOT_EXISTED));

        try {
            if (!book.getIsbn().equals(bookCreateRequest.getIsbn())) {
                if (bookRepository.findByIsbn(bookCreateRequest.getIsbn()).isPresent()) {
                    throw new AppException(ErrorCode.BOOK_EXISTED);
                }
            }

            bookMapper.updateBook(book, bookCreateRequest);
            book.setType(type);
            return bookMapper.toBookResponse(bookRepository.save(book));
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }


    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
        
        boolean isBorrowed = borrowingRepository.existsByBookAndReturnDateIsNull(book);
        if (isBorrowed) {
            throw new AppException(ErrorCode.BOOK_IS_CURRENTLY_BORROWED);
        }

        try {
            bookRepository.delete(book);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public Page<BookResponse> getBooks(Pageable pageable) {
        try {
            return mapBookPageBookResponsePage(bookRepository.findAll(pageable));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public Page<BookResponse> mapBookPageBookResponsePage(Page<Book> bookPage) {
        List<BookResponse> bookResponses = bookPage.getContent().stream()
            .map(book -> mapToBookResponseByMapper(book.getId()))
            .collect(Collectors.toList());

        return new PageImpl<>(bookResponses, bookPage.getPageable(), bookPage.getTotalElements());
	}
    public BookResponse mapToBookResponseByMapper(Long id) {

        Book book = bookRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));

        BookResponse bookResponse = bookMapper.toBookResponse(book);
        bookResponse.setBookType(bookTypeMapper.toBookTypeResponse(book.getType()));
        return bookResponse;
    }
    
    public BookResponse getBook(Long id) {
        try {
            return mapToBookResponseByMapper(id);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public Page<BookResponse> searchBooks(SearchBookRequest searchBookRequest, Pageable pageable) {
        try {
            return mapBookPageBookResponsePage(bookRepository.findAll(BookSpecification
                .filterBooks(searchBookRequest.getTitle(), 
                            searchBookRequest.getAuthor(), 
                            searchBookRequest.getTypeName(), 
                            searchBookRequest.getPublisher(), 
                            searchBookRequest.getPublishedDateFrom(), 
                            searchBookRequest.getPublishedDateTo(), 
                            searchBookRequest.getMaxBorrowDays(), 
                            searchBookRequest.getLocation(), 
                            searchBookRequest.getNameUserBrrow()), pageable));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public BorrowingResponse borrowBook(BorrowingRequest bRequest) {
        Book book = bookRepository.findById(bRequest.getBookId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        if (book.getStock() <= 0) {
            new AppException(ErrorCode.BOOK_OUT_OF_STOCK);
        }
        boolean alreadyBorrowed = borrowingRepository.existsByUserIdAndBookIdAndReturnDateIsNull(bRequest.getUserId(), bRequest.getBookId());
        if (alreadyBorrowed) {
            throw new AppException(ErrorCode.BOOK_ALREADY_BORROWED);
        }

        User user = userRepository.findById(bRequest.getUserId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        try {
            LocalDate borrowDate = LocalDate.now();
            LocalDate dueDate = borrowDate.plusDays(book.getMaxBorrowDays());

            Borrowing borrowing = Borrowing.builder()
                    .user(user)
                    .book(book)
                    .borrowDate(borrowDate)
                    .dueDate(dueDate)
                    .build();

            book.setStock(book.getStock() - 1);
            bookRepository.save(book);

            return  borrowwingMapper.toBorrowingResponse(borrowingRepository.save(borrowing));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
    public BorrowingResponse returnBook(BorrowingRequest bRequest) {
        Borrowing borrowing = borrowingRepository.findByUserIdAndBookIdAndReturnDateIsNull(bRequest.getUserId(), bRequest.getBookId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_BORROWED));
    
        LocalDate returnDate = LocalDate.now();
        borrowing.setReturnDate(returnDate);
        if (returnDate.isAfter(borrowing.getDueDate())) {
            throw new AppException(ErrorCode.BOOK_RETURN_LATE);
        }
    
        try {
            Book book = borrowing.getBook();
            book.setStock(book.getStock() + 1);
            bookRepository.save(book);
        
            return  borrowwingMapper.toBorrowingResponse(borrowingRepository.save(borrowing));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public Page<BookResponse> getBookBorrowByUser(Long userId, Pageable pageable) {
        List<Borrowing> borrowings = borrowingRepository.findByUserIdAndReturnDateIsNull(userId);
        if(borrowings.isEmpty()) throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);

        try {
            List<BookResponse> lstBook = borrowings.stream()
                    .map(b -> bookMapper.toBookResponse(b.getBook()))
                    .collect(Collectors.toList());

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), lstBook.size());
            List<BookResponse> pageContent = lstBook.subList(start, end);

            return new PageImpl<>(pageContent, pageable, lstBook.size());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Transactional
    public void importBooks(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("The file is empty. Please select a valid CSV file.");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("The file is too large. Maximum allowed size is 5MB.");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVParser csvParser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreSurroundingSpaces(true)
                    .build()
                    .parse(reader);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/d");

            Map<String, Book> booksToUpdate = new HashMap<>();
            List<Book> newBooks = csvParser.getRecords().stream().map(record -> {
                String isbn = record.get("isbn");
                return bookRepository.findByIsbn(isbn)
                        .map(book -> {
                            book.setStock(book.getStock() + Integer.parseInt(record.get("stock")));
                            booksToUpdate.put(isbn, book);
                            return book;
                        })
                        .orElseGet(() -> Book.builder()
                                .isbn(isbn)
                                .title(record.get("title"))
                                .author(record.get("author"))
                                .type(bookTypeRepository.findById(Long.parseLong(record.get("typeId")))
                                        .orElseThrow(() -> new AppException(ErrorCode.BOOKTYPE_NOT_EXISTED)))
                                .stock(Integer.parseInt(record.get("stock")))
                                .publisher(record.get("publisher"))
                                .publishedDate(LocalDate.parse(record.get("publishedDate"), formatter))
                                .maxBorrowDays(Integer.parseInt(record.get("maxBorrowDays")))
                                .location(record.get("location"))
                                .coverImageUrl(record.get("coverImageUrl"))
                                .build());
            }).collect(Collectors.toList());
            

            bookRepository.saveAll(booksToUpdate.values());
            bookRepository.saveAll(newBooks);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

}
