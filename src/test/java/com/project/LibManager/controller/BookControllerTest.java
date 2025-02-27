package com.project.LibManager.controller;

import java.time.LocalDate;
import java.util.Collections;

import com.project.LibManager.dto.response.BorrowingResponse;
import com.project.LibManager.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.LibManager.constant.ErrorCode;
import com.project.LibManager.dto.request.BookCreateRequest;
import com.project.LibManager.dto.request.BookUpdateRequest;
import com.project.LibManager.dto.response.BookResponse;
import com.project.LibManager.dto.response.BookTypeResponse;
import com.project.LibManager.exception.AppException;
import com.project.LibManager.service.IBookService;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IBookService iBookService;

    private BookUpdateRequest bookUpdateRequest;
    private BookCreateRequest bookCreateRequest;
    private BorrowingResponse borrowingResponse;
    private UserResponse userResponse;
    private BookResponse bookResponse;
    private LocalDate publishedDate;

    @BeforeEach
    void initData() {
        publishedDate = LocalDate.of(2020, 5, 15);

        bookCreateRequest = BookCreateRequest.builder()
                .title("Java Programming")
                .author("John Doe")
                .isbn("9781234567890")
                .typeId(1L)
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
                .typeId(1L)
                .stock(8)
                .publisher("Tech Books Publishing")
                .publishedDate(publishedDate)
                .maxBorrowDays(10)
                .location("B2-Section")
                .coverImageUrl("https://example.com/advanced-java-cover.jpg")
                .build();


        bookResponse = BookResponse.builder()
                .id(1L)
                .isbn("9781234567890")
                .title("Java Programming")
                .author("John Doe")
                .bookType( BookTypeResponse.builder().id(1L).name("Programming").build())
                .stock(10)
                .publisher("Tech Books Publishing")
                .publishedDate(publishedDate)
                .maxBorrowDays(14)
                .location("A1-Section")
                .coverImageUrl("https://example.com/java-cover.jpg")
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .email("testuser@example.com")
                .fullName("Le Trong An")
                .isVerified(true)
                .birthDate(LocalDate.of(2000, 5, 10))
                .build();

        borrowingResponse = BorrowingResponse.builder()
                .id(1L)
                .user(userResponse)
                .book(bookResponse)
                .borrowDate(LocalDate.of(2024, 2, 10))
                .dueDate(LocalDate.of(2024, 2, 24))
                .returnDate(null)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBook_success() throws Exception {
        //GIVEN
        ObjectMapper object = new ObjectMapper();
        object.registerModule(new JavaTimeModule());
        String content = object.writeValueAsString(bookCreateRequest);

        Mockito.when(iBookService.createBook(ArgumentMatchers.any())).thenReturn(bookResponse);
        //WHEN, THEN

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/books/admin")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(200)
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBook_valid_fail() throws Exception {
        bookCreateRequest.setIsbn("1232222221");
        //GIVEN
        ObjectMapper object = new ObjectMapper();
        object.registerModule(new JavaTimeModule());
        String content = object.writeValueAsString(bookCreateRequest);

        Mockito.when(iBookService.createBook(ArgumentMatchers.any())).thenReturn(bookResponse);
        //WHEN, THEN

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/books/admin")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1028))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("message")
                        .value("ISBN must be 13 characters"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBook_not_existed_type_fail() throws Exception {
        bookCreateRequest.setIsbn("9781234567890");
        //GIVEN
        ObjectMapper object = new ObjectMapper();
        object.registerModule(new JavaTimeModule());
        String content = object.writeValueAsString(bookCreateRequest);

        Mockito.when(iBookService.createBook(ArgumentMatchers.any())).thenThrow(new AppException(ErrorCode.BOOKTYPE_NOT_EXISTED));
     
        //WHEN, THEN

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/books/admin")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1016))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("message")
                        .value("Book type not existed"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateBook_success() throws Exception {
        //GIVEN
        Long bookId = 1L;
        ObjectMapper object = new ObjectMapper();
        object.registerModule(new JavaTimeModule());
        String content = object.writeValueAsString(bookUpdateRequest);

        BookResponse bookUpdateResponse = BookResponse.builder()
                .id(1L)
                .isbn("9780987654321")
                .title("Advanced Java")
                .author("John Doe")
                .bookType( BookTypeResponse.builder().id(1L).name("Programming").build())
                .stock(8)
                .publisher("Tech Books Publishing")
                .publishedDate(publishedDate)
                .maxBorrowDays(10)
                .location("B2-Section")
                .coverImageUrl("https://example.com/advanced-java-cover.jpg")
                .build();

        Mockito.when(iBookService.updateBook(ArgumentMatchers.any(), ArgumentMatchers.eq(bookId))).thenReturn(bookUpdateResponse);
        //WHEN, THEN

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/books/admin/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(200)
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateBook_notFound() throws Exception {
        // Given
        Long nonExistentBookId = 999L;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(bookUpdateRequest);

        Mockito.when(iBookService.updateBook(ArgumentMatchers.any(), ArgumentMatchers.eq(nonExistentBookId)))
                .thenThrow(new AppException(ErrorCode.BOOK_NOT_EXISTED));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/books/admin/" + nonExistentBookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ErrorCode.BOOK_NOT_EXISTED.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value(ErrorCode.BOOK_NOT_EXISTED.getMessage()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteBook_success() throws Exception {
        // Given
        Long bookId = 1L;

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/books/admin/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200));

        // Verify that the deleteBook method was called with the correct book ID
        Mockito.verify(iBookService, Mockito.times(1)).deleteBook(bookId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteBook_currentlyBorrowed_shouldReturnBadRequest() throws Exception {
        // Given
        Long borrowedBookId = 1L;
        Mockito.doThrow(new AppException(ErrorCode.BOOK_IS_CURRENTLY_BORROWED))
                .when(iBookService).deleteBook(borrowedBookId);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/books/admin/" + borrowedBookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ErrorCode.BOOK_IS_CURRENTLY_BORROWED.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value(ErrorCode.BOOK_IS_CURRENTLY_BORROWED.getMessage()));

        Mockito.verify(iBookService, Mockito.times(1)).deleteBook(borrowedBookId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getBooks_withDefaultParameters_shouldReturnPageOfBookResponses() throws Exception {
        // Given
        Page<BookResponse> mockPage = new PageImpl<>(Collections.singletonList(bookResponse));
        Mockito.when(iBookService.getBooks(ArgumentMatchers.any(Pageable.class))).thenReturn(mockPage);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/books/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result.content[0].id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("result.content[0].title").value("Java Programming"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.content[0].author").value("John Doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.content[0].isbn").value("9781234567890"));

        Mockito.verify(iBookService).getBooks(ArgumentMatchers.any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getBooks_withEmptyResultSet_shouldReturnEmptyPage() throws Exception {
        // Given
        Page<BookResponse> emptyPage = new PageImpl<>(Collections.emptyList());
        Mockito.when(iBookService.getBooks(ArgumentMatchers.any(Pageable.class))).thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/books/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result.content").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("result.totalElements").value(0));

        Mockito.verify(iBookService).getBooks(ArgumentMatchers.any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getBook_success() throws Exception {
        // Given
        Long bookId = 1L;

        Mockito.when(iBookService.getBook(bookId)).thenReturn(bookResponse);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/books/detail/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value(bookId))
                .andExpect(MockMvcResultMatchers.jsonPath("result.title").value("Java Programming"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.author").value("John Doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.isbn").value("9781234567890"));

        Mockito.verify(iBookService).getBook(bookId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getBook_nonExistentId_shouldReturnNotFound() throws Exception {
        // Given
        Long nonExistentBookId = 999L;
        Mockito.when(iBookService.getBook(nonExistentBookId)).thenThrow(new AppException(ErrorCode.BOOK_NOT_EXISTED));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/books/detail/" + nonExistentBookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ErrorCode.BOOK_NOT_EXISTED.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value(ErrorCode.BOOK_NOT_EXISTED.getMessage()));

        Mockito.verify(iBookService).getBook(nonExistentBookId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void borrowBooks_success() throws Exception {
        // Given
        Long bookId = 1L;
        Mockito.when(iBookService.borrowBook(bookId)).thenReturn(borrowingResponse);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/books/user/borrow/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("Borrow book is successfully!"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("result.user.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("result.book.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("result.borrowDate").value("2024-02-10"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.dueDate").value("2024-02-24"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.returnDate").doesNotExist());

        Mockito.verify(iBookService).borrowBook(bookId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void borrowBooks_alreadyBorrowedByUser_shouldReturnBadRequest() throws Exception {
        // Given
        Long bookId = 1L;
        Mockito.when(iBookService.borrowBook(bookId)).thenThrow(new AppException(ErrorCode.BOOK_IS_CURRENTLY_BORROWED));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/books/user/borrow/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ErrorCode.BOOK_IS_CURRENTLY_BORROWED.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value(ErrorCode.BOOK_IS_CURRENTLY_BORROWED.getMessage()));

        Mockito.verify(iBookService, Mockito.times(1)).borrowBook(bookId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void borrowBooks_outOfStock_shouldReturnBadRequest() throws Exception {
        // Given
        Long bookId = 1L;
        Mockito.when(iBookService.borrowBook(bookId)).thenThrow(new AppException(ErrorCode.BOOK_OUT_OF_STOCK));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/books/user/borrow/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ErrorCode.BOOK_OUT_OF_STOCK.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value(ErrorCode.BOOK_OUT_OF_STOCK.getMessage()));

        Mockito.verify(iBookService, Mockito.times(1)).borrowBook(bookId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void returnBooks_success() throws Exception {
        // Given
        Long bookId = 1L;
        BorrowingResponse borrowingResponse = BorrowingResponse.builder()
                .id(1L)
                .user(userResponse)
                .book(bookResponse)
                .borrowDate(LocalDate.of(2024, 2, 10))
                .dueDate(LocalDate.of(2024, 2, 24))
                .returnDate(LocalDate.now())
                .build();

        Mockito.when(iBookService.returnBook(bookId)).thenReturn(borrowingResponse);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/books/user/return/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("Return book is successfully!"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("result.returnDate").isNotEmpty());

        Mockito.verify(iBookService, Mockito.times(1)).returnBook(bookId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void returnBooks_bookNotBorrowed_shouldReturnBadRequest() throws Exception {
        // Given
        Long notBorrowedBookId = 2L;
        Mockito.when(iBookService.returnBook(notBorrowedBookId))
                .thenThrow(new AppException(ErrorCode.BOOK_NOT_BORROWED));

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/books/user/return/" + notBorrowedBookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ErrorCode.BOOK_NOT_BORROWED.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value(ErrorCode.BOOK_NOT_BORROWED.getMessage()));

        Mockito.verify(iBookService, Mockito.times(1)).returnBook(notBorrowedBookId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBookBorrowByUser_success() throws Exception {
        // Given
        Long userId = 1L;
        int offset = 0;
        int limit = 10;
        Page<BookResponse> mockPage = new PageImpl<>(Collections.singletonList(bookResponse));
        Mockito.when(iBookService.getBookBorrowByUser(ArgumentMatchers.eq(userId), ArgumentMatchers.any(Pageable.class)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/books/admin/borrow-by-user")
                        .param("userId", userId.toString())
                        .param("offset", String.valueOf(offset))
                        .param("limit", String.valueOf(limit))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result.content[0].id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("result.content[0].title").value("Java Programming"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.content[0].author").value("John Doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.content[0].isbn").value("9781234567890"));

        Mockito.verify(iBookService).getBookBorrowByUser(ArgumentMatchers.eq(userId), ArgumentMatchers.any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBookBorrowByUser_noBooksBorrowed_shouldReturnEmptyPage() throws Exception {
        // Given
        Long userId = 1L;
        Page<BookResponse> emptyPage = new PageImpl<>(Collections.emptyList());
        Mockito.when(iBookService.getBookBorrowByUser(ArgumentMatchers.eq(userId), ArgumentMatchers.any(Pageable.class)))
                .thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/books/admin/borrow-by-user")
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result.content").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("result.totalElements").value(0));

        Mockito.verify(iBookService).getBookBorrowByUser(ArgumentMatchers.eq(userId), ArgumentMatchers.any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void importBooks_withValidCsvFile_shouldReturnOkStatus() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "books.csv", "text/csv", "CSV content".getBytes()
        );

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/books/admin/import")
                        .file(file))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("Books imported successfully!"));

        Mockito.verify(iBookService, Mockito.times(1)).importBooks(ArgumentMatchers.any(MultipartFile.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void importBooks_shouldHandleLargeCsvFiles() throws Exception {
        // Given
        long fileSize = 50 * 1024 * 1024; // 50 MB
        MockMultipartFile largeCsvFile = new MockMultipartFile(
                "file",
                "large_books.csv",
                "text/csv",
                new byte[(int) fileSize]
        );

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/books/admin/import")
                        .file(largeCsvFile))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("Books imported successfully!"));

        Mockito.verify(iBookService).importBooks(ArgumentMatchers.any(MultipartFile.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void importBooks_exceedingMaxFileSize_shouldReturnBadRequest() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                new byte[50 * 1024 + 1]
        );

        Mockito.doThrow(new AppException(ErrorCode.FILE_LIMIT))
       .when(iBookService)
       .importBooks(file);
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/books/admin/import")
                        .file(file))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ErrorCode.FILE_LIMIT.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value(ErrorCode.FILE_LIMIT.getMessage()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void importBooks_unsupportedFileType_shouldReturnUnsupportedMediaType() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test content".getBytes()
        );

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/books/admin/import")
                        .file(file))
                .andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType())
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("error").value("UNSUPPORTED_MEDIA_TYPE"))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("Only CSV files are supported."));

        Mockito.verify(iBookService, Mockito.never()).importBooks(ArgumentMatchers.any(MultipartFile.class));
    }
}
