package com.project.libmanager.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.project.libmanager.service.dto.request.BookCreateRequest;
import com.project.libmanager.service.dto.request.BookUpdateRequest;
import com.project.libmanager.service.dto.response.BookResponse;
import com.project.libmanager.entity.Book;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookMapper {

    Book toBook(BookCreateRequest bookRequest);

    @Mapping(target = "bookType", source = "type")
    BookResponse toBookResponse(Book book);

    void updateBook(@MappingTarget Book book, BookUpdateRequest bookCreateRequest);

}
