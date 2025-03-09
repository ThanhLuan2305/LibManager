package com.project.libmanager.service.mapper;

import org.mapstruct.Mapper;

import com.project.libmanager.service.dto.response.BookTypeResponse;
import com.project.libmanager.entity.BookType;

@Mapper(componentModel = "spring")
public interface BookTypeMapper {
    BookTypeResponse toBookTypeResponse(BookType bookType);
}
