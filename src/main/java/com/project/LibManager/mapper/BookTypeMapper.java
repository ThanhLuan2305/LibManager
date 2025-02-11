package com.project.LibManager.mapper;

import org.mapstruct.Mapper;

import com.project.LibManager.dto.response.BookTypeResponse;
import com.project.LibManager.entity.BookType;

@Mapper(componentModel = "spring")
public interface BookTypeMapper {
    BookTypeResponse toBookTypeResponse(BookType bookType);
}
