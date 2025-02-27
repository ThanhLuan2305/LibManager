package com.project.LibManager.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


import com.project.LibManager.constant.ErrorCode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import com.project.LibManager.dto.request.BookCreateRequest;
import com.project.LibManager.dto.request.BookUpdateRequest;
import com.project.LibManager.dto.response.BookResponse;
import com.project.LibManager.dto.response.BookTypeResponse;
import com.project.LibManager.dto.response.BorrowingResponse;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.entity.Book;
import com.project.LibManager.entity.BookType;
import com.project.LibManager.entity.Borrowing;
import com.project.LibManager.entity.User;
import com.project.LibManager.exception.AppException;
import com.project.LibManager.mapper.BookMapper;
import com.project.LibManager.mapper.BookTypeMapper;
import com.project.LibManager.mapper.BorrowingMapper;
import com.project.LibManager.repository.BookRepository;
import com.project.LibManager.repository.BookTypeRepository;
import com.project.LibManager.repository.BorrowingRepository;
import com.project.LibManager.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@TestPropertySource("/test.properties")
public class BookServiceTest {
    @Autowired
    private IBookService iBookService;

    @MockitoBean
    private BookRepository bookRepository;
    @MockitoBean
    private BookTypeRepository bookTypeRepository;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private BookMapper bookMapper;
    @MockitoBean
    private BookTypeMapper bookTypeMapper;
    @MockitoBean
    private BorrowingRepository borrowingRepository;
    @MockitoBean
    private BorrowingMapper borrowwingMapper;
    

    private BookUpdateRequest bookUpdateRequest;
    private BookCreateRequest bookCreateRequest;
    private BorrowingResponse borrowingResponse;
    private UserResponse userResponse;
    private BookResponse bookResponse;
    private Book book;
    private User user;
    private BookType bookType;
    private Borrowing borrowing;
    private LocalDate publishedDate;

    @BeforeEach
    void initData() {
    publishedDate = LocalDate.of(2020, 5, 15);

    bookType = BookType.builder()
            .id(1L)
            .name("Programming")
            .build();

    user = User.builder()
            .id(16L)
            .email("lta@gmail.com")
            .fullName("Le Trong An")
            .isVerified(false)
            .birthDate(LocalDate.of(2000, 5, 10))
            .password("123123")
            .isDeleted(false)
            .build();

    book = Book.builder()
            .id(1L)
            .isbn("9781234567890")
            .title("Java Programming")
            .author("John Doe")
            .type(bookType)
            .stock(10)
            .publisher("Tech Books Publishing")
            .publishedDate(publishedDate)
            .maxBorrowDays(14)
            .location("A1-Section")
            .coverImageUrl("https://example.com/java-cover.jpg")
            .build();

    borrowing = Borrowing.builder()
            .id(1L)
            .user(user)
            .book(book)
            .borrowDate(LocalDate.of(2024, 2, 10))
            .dueDate(LocalDate.of(2024, 2, 24))
            .returnDate(null)
            .build();

    bookCreateRequest = BookCreateRequest.builder()
            .title("Java Programming")
            .author("John Doe")
            .isbn("9781234567890")
            .typeId(bookType.getId())
            .stock(10)
            .publisher("Tech Books Publishing")
            .publishedDate(publishedDate)
            .maxBorrowDays(14)
            .location("A1-Section")
            .coverImageUrl("https://example.com/java-cover.jpg")
            .build();

    bookUpdateRequest = BookUpdateRequest.builder()
            .title("Advanced Java")
            .author("John Doe")
            .isbn("9780987654321")
            .typeId(bookType.getId())
            .stock(8)
            .publisher("Tech Books Publishing")
            .publishedDate(publishedDate)
            .maxBorrowDays(10)
            .location("B2-Section")
            .coverImageUrl("https://example.com/advanced-java-cover.jpg")
            .build();

    bookResponse = BookResponse.builder()
            .id(book.getId())
            .isbn(book.getIsbn())
            .title(book.getTitle())
            .author(book.getAuthor())
            .bookType(BookTypeResponse.builder().id(1L).name("Programming").build())
            .stock(book.getStock())
            .publisher(book.getPublisher())
            .publishedDate(book.getPublishedDate())
            .maxBorrowDays(book.getMaxBorrowDays())
            .location(book.getLocation())
            .coverImageUrl(book.getCoverImageUrl())
            .build();

    userResponse = UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .isVerified(user.getIsVerified())
            .birthDate(user.getBirthDate())
            .build();

    borrowingResponse = BorrowingResponse.builder()
            .id(borrowing.getId())
            .user(userResponse)
            .book(bookResponse)
            .borrowDate(borrowing.getBorrowDate())
            .dueDate(borrowing.getDueDate())
            .returnDate(borrowing.getReturnDate())
            .build();
    }

    @Test
    void testCreateBookSuccess() {
        // GIVEN
        when(bookMapper.toBook(bookCreateRequest)).thenReturn(book);
        when(bookTypeRepository.findById(bookType.getId())).thenReturn(Optional.of(bookType));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookMapper.toBookResponse(book)).thenReturn(bookResponse);

        // WHEN
        BookResponse result = iBookService.createBook(bookCreateRequest);

        // THEN
        verify(bookMapper).toBook(bookCreateRequest);
        verify(bookTypeRepository).findById(bookType.getId());
        verify(bookRepository).save(any(Book.class));
        verify(bookMapper).toBookResponse(book);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getTitle()).isEqualTo(bookCreateRequest.getTitle());
        Assertions.assertThat(result.getAuthor()).isEqualTo(bookCreateRequest.getAuthor());
        Assertions.assertThat(result.getIsbn()).isEqualTo(bookCreateRequest.getIsbn());
        Assertions.assertThat(result.getBookType().getId()).isEqualTo(bookCreateRequest.getTypeId());
        Assertions.assertThat(result.getStock()).isEqualTo(bookCreateRequest.getStock());
        Assertions.assertThat(result.getPublisher()).isEqualTo(bookCreateRequest.getPublisher());
        Assertions.assertThat(result.getPublishedDate()).isEqualTo(bookCreateRequest.getPublishedDate());
        Assertions.assertThat(result.getMaxBorrowDays()).isEqualTo(bookCreateRequest.getMaxBorrowDays());
        Assertions.assertThat(result.getLocation()).isEqualTo(bookCreateRequest.getLocation());
        Assertions.assertThat(result.getCoverImageUrl()).isEqualTo(bookCreateRequest.getCoverImageUrl());
    }

    @Test
    void shouldUpdateStockWhenBookWithSameIsbnExists() {
        // Arrange
        BookCreateRequest bookCreateRequest = new BookCreateRequest();
        bookCreateRequest.setIsbn("123456789");
        bookCreateRequest.setStock(5);
        bookCreateRequest.setTypeId(1L);

        Book existingBook = new Book();
        existingBook.setIsbn("123456789");
        existingBook.setStock(10);

        BookType bookType = new BookType();
        bookType.setId(1L);

        when(bookTypeRepository.findById(1L)).thenReturn(Optional.of(bookType));
        when(bookRepository.findByIsbn("123456789")).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(existingBook);
        when(bookMapper.toBookResponse(any(Book.class))).thenReturn(new BookResponse());

        // Act
        BookResponse result = iBookService.createBook(bookCreateRequest);

        // Assert
        verify(bookRepository).save(existingBook);
        assertEquals(15, existingBook.getStock());
        assertNotNull(result);
    }

    @Test
    public void shouldCreateNewBookWhenIsbnDoesNotExist() {
        // Arrange
        BookCreateRequest bookCreateRequest = new BookCreateRequest();
        bookCreateRequest.setIsbn("1234567890");
        bookCreateRequest.setTypeId(1L);
        bookCreateRequest.setStock(5);

        BookType bookType = new BookType();
        bookType.setId(1L);

        Book newBook = new Book();
        newBook.setIsbn("1234567890");
        newBook.setStock(5);

        BookResponse expectedResponse = new BookResponse();
        expectedResponse.setIsbn("1234567890");
        expectedResponse.setStock(5);

        when(bookTypeRepository.findById(1L)).thenReturn(Optional.of(bookType));
        when(bookRepository.findByIsbn("1234567890")).thenReturn(Optional.empty());
        when(bookMapper.toBook(bookCreateRequest)).thenReturn(newBook);
        when(bookRepository.save(any(Book.class))).thenReturn(newBook);
        when(bookMapper.toBookResponse(newBook)).thenReturn(expectedResponse);

        // Act
        BookResponse result = iBookService.createBook(bookCreateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("1234567890", result.getIsbn());
        assertEquals(5, result.getStock());

        verify(bookTypeRepository).findById(1L);
        verify(bookRepository).findByIsbn("1234567890");
        verify(bookMapper).toBook(bookCreateRequest);
        verify(bookRepository).save(newBook);
        verify(bookMapper).toBookResponse(newBook);
    }

    @Test
    void createBook_shouldThrowAppException_whenDataAccessExceptionOccurs() {
        // Arrange
        BookCreateRequest bookCreateRequest = new BookCreateRequest();
        bookCreateRequest.setTypeId(1L);
        bookCreateRequest.setIsbn("1234567890");

        BookType bookType = new BookType();
        when(bookTypeRepository.findById(1L)).thenReturn(Optional.of(bookType));
        when(bookRepository.findByIsbn("1234567890")).thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        Assertions.assertThatThrownBy(() -> iBookService.createBook(bookCreateRequest))
                .isInstanceOf(AppException.class);

        verify(bookRepository).findByIsbn("1234567890");
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    public void shouldSuccessfullyUpdateBookWhenIsbnRemainsSame() {
        // Arrange
        Long bookId = 1L;
        BookUpdateRequest bookUpdateRequest = new BookUpdateRequest();
        bookUpdateRequest.setIsbn("1234567890");
        bookUpdateRequest.setTypeId(2L);

        Book existingBook = new Book();
        existingBook.setId(bookId);
        existingBook.setIsbn("1234567890");

        BookType newBookType = new BookType();
        newBookType.setId(2L);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookTypeRepository.findById(2L)).thenReturn(Optional.of(newBookType));
        when(bookRepository.save(any(Book.class))).thenReturn(existingBook);

        BookResponse expectedResponse = new BookResponse();
        when(bookMapper.toBookResponse(any(Book.class))).thenReturn(expectedResponse);

        // Act
        BookResponse result = iBookService.updateBook(bookUpdateRequest, bookId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(bookRepository).findById(bookId);
        verify(bookTypeRepository).findById(2L);
        verify(bookMapper).updateBook(eq(existingBook), eq(bookUpdateRequest));
        verify(bookRepository).save(existingBook);
        verify(bookMapper).toBookResponse(existingBook);
    }

    @Test
    void shouldUpdateBookSuccessfullyWhenIsbnChangesToNewUniqueValue() {
        // Arrange
        Long bookId = 1L;
        BookUpdateRequest bookUpdateRequest = new BookUpdateRequest();
        bookUpdateRequest.setIsbn("9876543210");
        bookUpdateRequest.setTypeId(2L);

        Book existingBook = new Book();
        existingBook.setId(bookId);
        existingBook.setIsbn("1234567890");

        BookType newType = new BookType();
        newType.setId(2L);

        Book updatedBook = new Book();
        updatedBook.setId(bookId);
        updatedBook.setIsbn("9876543210");
        updatedBook.setType(newType);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookTypeRepository.findById(2L)).thenReturn(Optional.of(newType));
        when(bookRepository.findByIsbn("9876543210")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);
        when(bookMapper.toBookResponse(updatedBook)).thenReturn(new BookResponse());

        // Act
        BookResponse result = iBookService.updateBook(bookUpdateRequest, bookId);

        // Assert
        assertNotNull(result);
        verify(bookRepository).findById(bookId);
        verify(bookTypeRepository).findById(2L);
        verify(bookRepository).findByIsbn("9876543210");
        verify(bookMapper).updateBook(existingBook, bookUpdateRequest);
        verify(bookRepository).save(existingBook);
        verify(bookMapper).toBookResponse(updatedBook);
    }

    @Test
    void updateBook_shouldThrowAppException_whenBookTypeDoesNotExist() {
        // GIVEN
        Long bookId = 1L;
        BookUpdateRequest bookUpdateRequest = BookUpdateRequest.builder()
                .typeId(2L)
                .isbn("1234567890")
                .title("Updated Book")
                .author("Updated Author")
                .build();

        Book existingBook = Book.builder()
                .id(bookId)
                .isbn("1234567890")
                .build();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookTypeRepository.findById(2L)).thenReturn(Optional.empty());

        // WHEN
        AppException exception = assertThrows(AppException.class, () -> iBookService.updateBook(bookUpdateRequest, bookId));

        // THEN
        Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BOOKTYPE_NOT_EXISTED);
        verify(bookRepository).findById(bookId);
        verify(bookTypeRepository).findById(2L);
        verifyNoMoreInteractions(bookRepository, bookTypeRepository, bookMapper);
    }

    @Test
    void updateBook_shouldThrowAppException_whenBookDoesNotExist() {
        // Arrange
        Long bookId = 1L;
        BookUpdateRequest bookUpdateRequest = new BookUpdateRequest();
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> iBookService.updateBook(bookUpdateRequest, bookId));
        assertEquals(ErrorCode.BOOK_NOT_EXISTED, exception.getErrorCode());
        verify(bookRepository).findById(bookId);
    }

    @Test
    void updateBook_shouldThrowAppException_whenBookUpdateRequestIsNull() {
        // Arrange
        Long bookId = 1L;

        // Act & Assert
        Assertions.assertThatThrownBy(() -> iBookService.updateBook(null, bookId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_KEY);
    }

    @Test
    void updateBook_shouldThrowAppException_whenIsbnAlreadyExists() {
        // Arrange
        Long bookId = 1L;
        BookUpdateRequest request = new BookUpdateRequest();
        request.setIsbn("123456789");
        request.setTypeId(1L);

        Book existingBook = new Book();
        existingBook.setId(bookId);
        existingBook.setIsbn("987654321");

        BookType existingType = new BookType();
        existingType.setId(1L);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookTypeRepository.findById(request.getTypeId())).thenReturn(Optional.of(existingType));
        when(bookRepository.findByIsbn(request.getIsbn())).thenReturn(Optional.of(new Book()));

        // Act & Assert
        Assertions.assertThatThrownBy(() -> iBookService.updateBook(request, bookId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOK_EXISTED);
    }


    @Test
    void updateBook_shouldThrowAppException_whenDataAccessExceptionOccurs() {
        // Arrange
        Long bookId = 1L;
        BookUpdateRequest request = new BookUpdateRequest();
        request.setIsbn("123456789");
        request.setTypeId(1L);

        Book existingBook = new Book();
        existingBook.setId(bookId);
        existingBook.setIsbn("123456789");

        BookType bookType = new BookType();
        bookType.setId(1L);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookTypeRepository.findById(request.getTypeId())).thenReturn(Optional.of(bookType));
        when(bookRepository.save(any(Book.class))).thenThrow(new DataAccessException("DB error") {});

        // Act & Assert
        Assertions.assertThatThrownBy(() -> iBookService.updateBook(request, bookId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNCATEGORIZED_EXCEPTION);
    }

    @Test
    void deleteBook_shouldDeleteBookSuccessfully_whenBookExistsAndNotBorrowed() {
        // Arrange
        Long bookId = 1L;
        Book book = new Book();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowingRepository.existsByBookAndReturnDateIsNull(book)).thenReturn(false);

        // Act
        iBookService.deleteBook(bookId);

        // Assert
        verify(bookRepository).delete(book);
    }

    @Test
    void deleteBook_shouldThrowAppException_whenBookIsCurrentlyBorrowed() {
        // Arrange
        Long bookId = 1L;
        Book book = new Book();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowingRepository.existsByBookAndReturnDateIsNull(book)).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> iBookService.deleteBook(bookId));
        assertEquals(ErrorCode.BOOK_IS_CURRENTLY_BORROWED, exception.getErrorCode());

        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    void deleteBook_BookNotExisted_ThrowsAppException() {
        // Arrange
        Long nonExistentBookId = 999L;
        when(bookRepository.findById(nonExistentBookId)).thenThrow(new AppException(ErrorCode.BOOK_NOT_EXISTED));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> iBookService.deleteBook(nonExistentBookId));
        assertEquals(ErrorCode.BOOK_NOT_EXISTED, exception.getErrorCode());
        verify(bookRepository, times(1)).findById(nonExistentBookId);
        verify(borrowingRepository, never()).existsByBookAndReturnDateIsNull(any());
    }

    @Test
    void deleteBook_WhenBookDoesNotExist_ShouldThrowAppException() {
        // Arrange
        Long nonExistentBookId = 999L;
        when(bookRepository.findById(nonExistentBookId)).thenThrow(new AppException(ErrorCode.BOOK_NOT_EXISTED));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> iBookService.deleteBook(nonExistentBookId));
        assertEquals(ErrorCode.BOOK_NOT_EXISTED, exception.getErrorCode());
        verify(bookRepository, times(1)).findById(nonExistentBookId);
        verify(borrowingRepository, never()).existsByBookAndReturnDateIsNull(any());
    }

    @Test
    void deleteBook_shouldThrowAppException_whenBookDoesNotExist() {
        // Arrange
        Long bookId = 1L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThatThrownBy(() -> iBookService.deleteBook(bookId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOK_NOT_EXISTED);
    }

    @Test
    void deleteBook_shouldThrowAppException_whenRepositoryThrowsException() {
        // Arrange
        Long bookId = 1L;
        Book book = new Book();
        book.setId(bookId);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowingRepository.existsByBookAndReturnDateIsNull(book)).thenReturn(false);
        doThrow(new RuntimeException("Database error")).when(bookRepository).delete(book);

        // Act & Assert
        Assertions.assertThatThrownBy(() -> iBookService.deleteBook(bookId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNCATEGORIZED_EXCEPTION);
    }

    @Test
    void shouldReturnPageOfBooksWhenRepositoryContainsBooks() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = Arrays.asList(
                Book.builder().id(1L).build(),
                Book.builder().id(2L).build(),
                Book.builder().id(3L).build()
        );
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);

        List<BookResponse> bookResponses = books.stream()
                .map(book -> {
                    BookResponse response = new BookResponse();
                    response.setId(book.getId());
                    return response;
                })
                .collect(Collectors.toList());
        Page<BookResponse> expectedResponse = new PageImpl<>(bookResponses, pageable, books.size());

        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(new Book()));
        when(bookMapper.toBookResponse(any(Book.class))).thenReturn(new BookResponse());
        when(bookTypeMapper.toBookTypeResponse(any())).thenReturn(new BookTypeResponse());

        // Act
        Page<BookResponse> result = iBookService.getBooks(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse.getTotalElements(), result.getTotalElements());
        assertEquals(expectedResponse.getNumber(), result.getNumber());
        assertEquals(expectedResponse.getSize(), result.getSize());
        verify(bookRepository).findAll(pageable);
        verify(bookRepository, times(3)).findById(anyLong());
        verify(bookMapper, times(3)).toBookResponse(any(Book.class));
        verify(bookTypeMapper, times(3)).toBookTypeResponse(any());
    }

    @Test
    void getBooks_shouldThrowAppException_whenBookRepositoryReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.findAll(pageable)).thenReturn(Page.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> iBookService.getBooks(pageable));
        assertEquals(ErrorCode.BOOK_NOT_EXISTED, exception.getErrorCode());
        verify(bookRepository).findAll(pageable);
    }

    @Test
    void getBook_shouldThrowAppException_whenBookDoesNotExist() {
        // Arrange
        Long nonExistentBookId = 999L;
        when(bookRepository.findById(nonExistentBookId)).thenThrow(new AppException(ErrorCode.BOOK_NOT_EXISTED));

        // Act & Assert
        assertThrows(AppException.class, () -> iBookService.getBook(nonExistentBookId));
        verify(bookRepository).findById(nonExistentBookId);
    }

    @Test
    void getBook_success() {
        // Arrange
        Long bookId = 1L;
        Book book = Book.builder()
                .id(bookId)
                .isbn("9781234567890")
                .title("Java Programming")
                .author("John Doe")
                .build();
        BookResponse expectedResponse = BookResponse.builder()
                .id(bookId)
                .isbn("9781234567890")
                .title("Java Programming")
                .author("John Doe")
                .build();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookMapper.toBookResponse(book)).thenReturn(expectedResponse);

        // Act
        BookResponse actualResponse = iBookService.getBook(bookId);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(bookRepository).findById(bookId);
        verify(bookMapper).toBookResponse(book);
    }

    @Test
    void borrowBook_shouldThrowUnauthorizedExceptionWhenSecurityContextIsNull() {
        // Arrange
        Long bookId = 1L;
        SecurityContextHolder.clearContext();

        // Act & Assert
        Assertions.assertThatThrownBy(() -> iBookService.borrowBook(bookId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);
    }

    @Test
    void borrowBook_shouldThrowAppException_whenUserNotFound() {
        // Arrange
        Long bookId = 1L;
        String userEmail = "test@example.com";

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userEmail);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(userEmail)).thenReturn(null);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> iBookService.borrowBook(bookId));
        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    void borrowBook_shouldThrowAppException_whenBookIsOutOfStock() {
        // Arrange
        Long bookId = 1L;
        Book book = new Book();
        book.setId(bookId);
        book.setStock(0);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(authentication.isAuthenticated()).thenReturn(true);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> iBookService.borrowBook(bookId));
        assertEquals(ErrorCode.BOOK_OUT_OF_STOCK, exception.getErrorCode());
    }

    @Test
    void borrowBook_shouldThrowAppException_whenUserAlreadyBorrowBook() {
        // Arrange
        Long bookId = 1L;
        Book book = new Book();
        book.setId(bookId);
        book.setStock(2);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(authentication.isAuthenticated()).thenReturn(true);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        when(borrowingRepository.existsByUserIdAndBookIdAndReturnDateIsNull(user.getId(), bookId)).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> iBookService.borrowBook(bookId));
        assertEquals(ErrorCode.BOOK_ALREADY_BORROWED, exception.getErrorCode());
    }

    @Test
    void shouldHandleExceptionDuringBorrowingProcessAndThrowAppException() {
        // Arrange
        Long bookId = 1L;
        User mockUser = new User();
        mockUser.setId(1L);
        Book mockBook = new Book();
        mockBook.setId(bookId);
        mockBook.setStock(1);
        mockBook.setMaxBorrowDays(14);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(authentication.isAuthenticated()).thenReturn(true);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(mockBook));
        when(borrowingRepository.existsByUserIdAndBookIdAndReturnDateIsNull(mockUser.getId(), bookId)).thenReturn(false);
        when(borrowingRepository.save(any(Borrowing.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        Assertions.assertThatThrownBy(() -> iBookService.borrowBook(bookId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNCATEGORIZED_EXCEPTION);

        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void returnBook_shouldReturnSuccessfully_whenValidRequest() {
        // Arrange
        Long bookId = 1L;
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        Book mockBook = new Book();
        mockBook.setId(bookId);
        mockBook.setStock(3);

        Borrowing mockBorrowing = new Borrowing();
        mockBorrowing.setUser(mockUser);
        mockBorrowing.setBook(mockBook);
        mockBorrowing.setBorrowDate(LocalDate.now().minusDays(5));
        mockBorrowing.setDueDate(LocalDate.now().plusDays(5));

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getName()).thenReturn("test@example.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(borrowingRepository.findByUserIdAndBookIdAndReturnDateIsNull(mockUser.getId(), bookId))
                .thenReturn(Optional.of(mockBorrowing));
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(mockBorrowing);
        when(bookRepository.save(any(Book.class))).thenReturn(mockBook);
        when(borrowwingMapper.toBorrowingResponse(any())).thenReturn(new BorrowingResponse());

        // Act
        BorrowingResponse response = iBookService.returnBook(bookId);

        // Assert
        assertNotNull(response);
        assertEquals(4, mockBook.getStock());
        verify(bookRepository, times(1)).save(mockBook);
        verify(borrowingRepository, times(1)).save(mockBorrowing);
    }

    @Test
    void returnBook_shouldThrowAppException_whenUserNotAuthenticated() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act & Assert
        Assertions.assertThatThrownBy(() -> iBookService.returnBook(1L))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);
    }

    @Test
    void returnBook_shouldThrowAppException_whenUserDoesNotExist() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getName()).thenReturn("test@example.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(null);

        // Act & Assert
        Assertions.assertThatThrownBy(() -> iBookService.returnBook(1L))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_EXISTED);
    }

    @Test
    void returnBook_shouldThrowAppException_whenBookNotBorrowed() {
        // Arrange
        Long bookId = 1L;
        User mockUser = new User();
        mockUser.setId(1L);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getName()).thenReturn("test@example.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(borrowingRepository.findByUserIdAndBookIdAndReturnDateIsNull(mockUser.getId(), bookId))
                .thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThatThrownBy(() -> iBookService.returnBook(bookId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOK_NOT_BORROWED);
    }

    @Test
    void returnBook_shouldThrowAppException_whenReturnLate() {
        // Arrange
        Long bookId = 1L;
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        Book mockBook = new Book();
        mockBook.setId(bookId);
        mockBook.setStock(3);

        Borrowing mockBorrowing = new Borrowing();
        mockBorrowing.setUser(mockUser);
        mockBorrowing.setBook(mockBook);
        mockBorrowing.setBorrowDate(LocalDate.now().minusDays(20));
        mockBorrowing.setDueDate(LocalDate.now().minusDays(5));

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getName()).thenReturn("test@example.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(borrowingRepository.findByUserIdAndBookIdAndReturnDateIsNull(mockUser.getId(), bookId))
                .thenReturn(Optional.of(mockBorrowing));

        // Act & Assert
        Assertions.assertThatThrownBy(() -> iBookService.returnBook(bookId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOK_RETURN_LATE);
    }

    @Test
    void returnBook_shouldHandleExceptionDuringProcessAndThrowAppException() {
        // Arrange
        Long bookId = 1L;
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        Book mockBook = new Book();
        mockBook.setId(bookId);
        mockBook.setStock(3);

        Borrowing mockBorrowing = new Borrowing();
        mockBorrowing.setUser(mockUser);
        mockBorrowing.setBook(mockBook);
        mockBorrowing.setBorrowDate(LocalDate.now().minusDays(5));
        mockBorrowing.setDueDate(LocalDate.now().plusDays(5));

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getName()).thenReturn("test@example.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(borrowingRepository.findByUserIdAndBookIdAndReturnDateIsNull(mockUser.getId(), bookId))
                .thenReturn(Optional.of(mockBorrowing));

        when(borrowingRepository.save(any(Borrowing.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        Assertions.assertThatThrownBy(() -> iBookService.returnBook(bookId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNCATEGORIZED_EXCEPTION);

        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void getBookBorrowByUser_shouldReturnPageOfBooks_whenUserHasBorrowedBooks() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 2);

        User mockUser = new User();
        mockUser.setId(userId);

        Book mockBook1 = new Book();
        mockBook1.setId(1L);
        mockBook1.setTitle("Book 1");

        Book mockBook2 = new Book();
        mockBook2.setId(2L);
        mockBook2.setTitle("Book 2");

        Borrowing borrowing1 = new Borrowing();
        borrowing1.setUser(mockUser);
        borrowing1.setBook(mockBook1);

        Borrowing borrowing2 = new Borrowing();
        borrowing2.setUser(mockUser);
        borrowing2.setBook(mockBook2);

        List<Borrowing> borrowings = Arrays.asList(borrowing1, borrowing2);

        BookResponse bookResponse1 = new BookResponse();
        bookResponse1.setId(1L);
        bookResponse1.setTitle("Book 1");

        BookResponse bookResponse2 = new BookResponse();
        bookResponse2.setId(2L);
        bookResponse2.setTitle("Book 2");

        when(borrowingRepository.findByUserIdAndReturnDateIsNull(userId)).thenReturn(borrowings);
        when(bookMapper.toBookResponse(mockBook1)).thenReturn(bookResponse1);
        when(bookMapper.toBookResponse(mockBook2)).thenReturn(bookResponse2);

        // Act
        Page<BookResponse> result = iBookService.getBookBorrowByUser(userId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("Book 1", result.getContent().get(0).getTitle());
        assertEquals("Book 2", result.getContent().get(1).getTitle());
    }

    @Test
    void getBookBorrowByUser_shouldThrowAppException_whenUserHasNoBorrowedBooks() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 2);

        when(borrowingRepository.findByUserIdAndReturnDateIsNull(userId)).thenReturn(Collections.emptyList());

        // Act & Assert
        Assertions.assertThatThrownBy(() -> iBookService.getBookBorrowByUser(userId, pageable))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNCATEGORIZED_EXCEPTION);
    }

    @Test
    void getBookBorrowByUser_shouldThrowAppException_whenUnexpectedExceptionOccurs() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 2);

        User mockUser = new User();
        mockUser.setId(userId);

        Book mockBook = new Book();
        mockBook.setId(1L);
        mockBook.setTitle("Book 1");

        Borrowing borrowing = new Borrowing();
        borrowing.setUser(mockUser);
        borrowing.setBook(mockBook);

        List<Borrowing> borrowings = Collections.singletonList(borrowing);

        when(borrowingRepository.findByUserIdAndReturnDateIsNull(userId)).thenReturn(borrowings);
        when(bookMapper.toBookResponse(mockBook)).thenThrow(new RuntimeException("Unexpected database error"));

        // Act & Assert
        Assertions.assertThatThrownBy(() -> iBookService.getBookBorrowByUser(userId, pageable))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNCATEGORIZED_EXCEPTION);
    }


    @Test
    void importBooks_shouldThrowAppException_whenFileIsEmpty() {
        // Arrange
        MultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> iBookService.importBooks(emptyFile));

        assertEquals(ErrorCode.FILE_EMPTY, exception.getErrorCode());
    }

    @Test
    void importBooks_shouldThrowAppException_whenFileExceedsSizeLimit() {
        // Arrange
        byte[] oversizedData = new byte[6 * 1024]; // 6KB > 5KB limit
        MultipartFile oversizedFile = new MockMultipartFile("file", "books.csv", "text/csv", oversizedData);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> iBookService.importBooks(oversizedFile));

        assertEquals(ErrorCode.FILE_LIMIT, exception.getErrorCode());
    }

    @Test
    void importBooks_shouldSaveBooksCorrectly_whenFileIsValid() throws Exception {
        // Arrange
        String csvData = """
                isbn,title,author,typeId,stock,publisher,publishedDate,maxBorrowDays,location,coverImageUrl
                123456,Book A,Author A,1,10,Publisher A,2023/01/01,14,Shelf A,coverA.jpg
                789012,Book B,Author B,2,5,Publisher B,2022/05/05,21,Shelf B,coverB.jpg
                """;
        MultipartFile file = new MockMultipartFile("file", "books.csv", "text/csv", csvData.getBytes(StandardCharsets.UTF_8));
        
        BookType mockType1 = BookType.builder().id(1l).name("Fiction").build();;
        BookType mockType2 = BookType.builder().id(1l).name("Fiction").build();;

        when(bookTypeRepository.findById(1L)).thenReturn(Optional.of(mockType1));
        when(bookTypeRepository.findById(2L)).thenReturn(Optional.of(mockType2));
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());

        // Act
        iBookService.importBooks(file);

        // Assert
        verify(bookRepository, times(1)).saveAll(anyList());
    }

    @Test
    void importBooks_shouldThrowAppException_whenBookTypeNotExists() throws Exception {
        // Arrange
        String csvData = """
                isbn,title,author,typeId,stock,publisher,publishedDate,maxBorrowDays,location,coverImageUrl
                123456,Book A,Author A,999,10,Publisher A,2023/01/01,14,Shelf A,coverA.jpg
                """;
        MultipartFile file = new MockMultipartFile("file", "books.csv", "text/csv", csvData.getBytes(StandardCharsets.UTF_8));

        when(bookTypeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> iBookService.importBooks(file));

        assertEquals(ErrorCode.BOOKTYPE_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void importBooks_shouldThrowAppException_whenUnexpectedExceptionOccurs() throws Exception {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getInputStream()).thenThrow(new IOException("File error"));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> iBookService.importBooks(file));

        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION, exception.getErrorCode());
    }
}
