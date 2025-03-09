package com.project.libmanager.service.mapper;

import org.mapstruct.Mapper;

import com.project.libmanager.service.dto.response.BorrowingResponse;
import com.project.libmanager.entity.Borrowing;

@Mapper(componentModel = "spring", uses = { UserMapper.class, BookMapper.class })
public interface BorrowingMapper {

    BorrowingResponse toBorrowingResponse(Borrowing borrowing);
}
