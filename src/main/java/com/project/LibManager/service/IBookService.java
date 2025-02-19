package com.project.LibManager.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.project.LibManager.dto.request.BookCreateRequest;
import com.project.LibManager.dto.request.BookUpdateRequest;
import com.project.LibManager.dto.request.SearchBookRequest;
import com.project.LibManager.dto.response.BookResponse;
import com.project.LibManager.dto.response.BorrowingResponse;
import com.project.LibManager.entity.Book;

public interface IBookService {

    public BookResponse createBook(BookCreateRequest bookCreateRequest);

    public BookResponse updateBook(BookUpdateRequest bookUpdateRequest, Long bookId);

    public void deleteBook(Long id);

    public Page<BookResponse> getBooks(Pageable pageable);

    public Page<BookResponse> mapBookPageBookResponsePage(Page<Book> bookPage);

    public BookResponse mapToBookResponseByMapper(Long id);
    
    public BookResponse getBook(Long id);

    public Page<BookResponse> searchBooks(SearchBookRequest searchBookRequest, Pageable pageable);

    public BorrowingResponse borrowBook(Long bookId);

    public BorrowingResponse returnBook(Long bookId);

    public Page<BookResponse> getBookBorrowByUser(Long userId, Pageable pageable);

    public void importBooks(MultipartFile file);

}
