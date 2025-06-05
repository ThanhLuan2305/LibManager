package com.project.libmanager.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.project.libmanager.service.dto.request.RegisterRequest;
import com.project.libmanager.service.dto.request.UserCreateRequest;
import com.project.libmanager.service.dto.request.UserUpdateRequest;
import com.project.libmanager.service.dto.response.UserResponse;
import com.project.libmanager.entity.User;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User toUser(UserCreateRequest request);

    UserResponse toUserResponse(User user);

    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    User fromRegisterRequest(RegisterRequest request);
}
