package com.project.LibManager.mapper;

import org.mapstruct.Mapper;

import com.project.LibManager.dto.response.BorrowingResponse;
import com.project.LibManager.entity.Borrowing;

@Mapper(componentModel = "spring", uses = { UserMapper.class, BookMapper.class })
public interface BorrowingMapper {
    
    BorrowingResponse toBorrowingResponse(Borrowing borrowing);
}
