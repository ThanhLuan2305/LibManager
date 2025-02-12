package com.project.LibManager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.project.LibManager.dto.response.BorrowingResponse;
import com.project.LibManager.entity.Borrowing;

@Mapper(componentModel = "spring", uses = { UserMapper.class, BookMapper.class })
public interface BorrowwingMapper {
    @Mapping(target = "user", source = "user")
    @Mapping(target = "book", source = "book")
    BorrowingResponse toBorrowingResponse(Borrowing borrowing);
}
