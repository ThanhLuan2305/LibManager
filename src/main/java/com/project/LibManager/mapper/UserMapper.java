package com.project.LibManager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.project.LibManager.dto.request.RegisterRequest;
import com.project.LibManager.dto.request.UserCreateRequest;
import com.project.LibManager.dto.request.UserUpdateRequest;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "borrowings", ignore = true)
    @Mapping(target = "lateReturnCount", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(UserCreateRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "borrowings", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "email", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "borrowings", ignore = true)
    @Mapping(target = "lateReturnCount", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "isReset", ignore = true)
    @Mapping(target = "isVerified", ignore = true)
    User fromRegisterRequest(RegisterRequest request);
}
