package com.project.LibManager.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

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
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponse createBook(BookCreateRequest bookCreateRequest) {
        if (bookCreateRequest == null) {
            log.error("BookCreateRequest is null");
            throw new AppException(ErrorCode.INVALID_KEY);
        }
        
        BookType type = bookTypeRepository.findById(bookCreateRequest.getTypeId())
        .orElseThrow(() -> new AppException(ErrorCode.BOOKTYPE_NOT_EXISTED));

        try {
            Book book = bookMapper.toBook(bookCreateRequest);
            book.setType(type);
            bookRepository.save(book);   
            return bookMapper.toBookResponse(book);
        } catch (DataAccessException e) {
            log.error("Database error: " + e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
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
        Set<Borrowing> setBorrow = book.getBorrowings();
        setBorrow.clear();
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
    
}
