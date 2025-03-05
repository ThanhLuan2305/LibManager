package com.project.LibManager.service.mapper;

import org.mapstruct.Mapper;

import com.project.LibManager.service.dto.response.BookTypeResponse;
import com.project.LibManager.entity.BookType;

@Mapper(componentModel = "spring")
public interface BookTypeMapper {
    BookTypeResponse toBookTypeResponse(BookType bookType);
}
