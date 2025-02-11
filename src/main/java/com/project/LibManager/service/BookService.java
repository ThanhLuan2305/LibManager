package com.project.LibManager.service;

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
import com.project.LibManager.dto.request.SearchBookRequest;
import com.project.LibManager.dto.response.BookResponse;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.entity.Book;
import com.project.LibManager.entity.BookType;
import com.project.LibManager.entity.Borrowing;
import com.project.LibManager.entity.User;
import com.project.LibManager.exception.AppException;
import com.project.LibManager.exception.ErrorCode;
import com.project.LibManager.mapper.BookMapper;
import com.project.LibManager.mapper.BookTypeMapper;
import com.project.LibManager.repository.BookRepository;
import com.project.LibManager.repository.BookTypeRepository;
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
}
