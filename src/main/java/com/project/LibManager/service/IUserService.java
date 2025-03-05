package com.project.LibManager.service;

import com.project.LibManager.criteria.UserCriteria;
import com.project.LibManager.service.dto.request.UserCreateRequest;
import com.project.LibManager.service.dto.request.UserUpdateRequest;
import com.project.LibManager.service.dto.response.UserResponse;
import com.project.LibManager.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {

    UserResponse createUser(UserCreateRequest request);

    Page<UserResponse> getUsers(Pageable pageable);

    Page<UserResponse> mapUserPageUserResponsePage(Page<User> userPage);

    UserResponse mapToUserResponseByMapper(Long id);

    UserResponse getUser(Long id);

    UserResponse getMyInfo();

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long userId);

    Page<UserResponse> searchUSer(UserCriteria criteria, Pageable pageable);
}
