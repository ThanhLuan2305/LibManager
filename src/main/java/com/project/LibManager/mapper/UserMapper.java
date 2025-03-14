package com.project.LibManager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.project.LibManager.dto.request.RegisterRequest;
import com.project.LibManager.dto.request.UserCreateRequest;
import com.project.LibManager.dto.request.UserUpdateRequest;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreateRequest request);

    UserResponse toUserResponse(User user);

    void updateUser(@MappingTarget User user, UserUpdateRequest request);
    
    User fromRegisterRequest(RegisterRequest request);
}
