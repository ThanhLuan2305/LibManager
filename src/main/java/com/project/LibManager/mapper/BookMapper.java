package com.project.LibManager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.project.LibManager.dto.request.BookCreateRequest;
import com.project.LibManager.dto.request.BookUpdateRequest;
import com.project.LibManager.dto.response.BookResponse;
import com.project.LibManager.entity.Book;

@Mapper(componentModel = "spring")
public interface BookMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "borrowings", ignore = true)
    @Mapping(target = "type", ignore = true)
    Book toBook(BookCreateRequest bookRequest);

    @Mapping(target = "bookType", source = "type")
    BookResponse toBookResponse(Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "borrowings", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "type", ignore = true)
    void updateBook(@MappingTarget Book book, BookUpdateRequest bookCreateRequest);

}
