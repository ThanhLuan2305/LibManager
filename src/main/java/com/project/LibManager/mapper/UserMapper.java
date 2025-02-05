package com.project.LibManager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.project.LibManager.dto.request.UserCreateRequest;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreateRequest request);

    UserResponse toUserResponse(User user);
	
}
