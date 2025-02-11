package com.project.LibManager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.project.LibManager.dto.request.BookCreateRequest;
import com.project.LibManager.dto.response.BookResponse;
import com.project.LibManager.entity.Book;

@Mapper(componentModel = "spring")
public interface BookMapper {
    Book toBook(BookCreateRequest bookRequest);
    
    BookResponse toBookResponse(Book book);

    void updateBook(@MappingTarget Book book, BookCreateRequest bookCreateRequest);
}
