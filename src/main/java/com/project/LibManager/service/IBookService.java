package com.project.LibManager.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.project.LibManager.dto.request.BookCreateRequest;
import com.project.LibManager.dto.request.BookUpdateRequest;
import com.project.LibManager.dto.response.BookResponse;
import com.project.LibManager.dto.response.BorrowingResponse;
import com.project.LibManager.entity.Book;

public interface IBookService {

    BookResponse createBook(BookCreateRequest bookCreateRequest);

    BookResponse updateBook(BookUpdateRequest bookUpdateRequest, Long bookId);

    void deleteBook(Long id);

    Page<BookResponse> getBooks(Pageable pageable);

    Page<BookResponse> mapBookPageBookResponsePage(Page<Book> bookPage);

    BookResponse mapToBookResponseByMapper(Long id);

    BookResponse getBook(Long id);

    BorrowingResponse borrowBook(Long bookId);

    BorrowingResponse returnBook(Long bookId);

    Page<BookResponse> getBookBorrowByUser(Long userId, Pageable pageable);

    Page<BookResponse> getBookBorrowForUser(Pageable pageable);

    void importBooks(MultipartFile file);

}
