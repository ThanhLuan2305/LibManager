package com.project.libmanager.service;

import com.project.libmanager.criteria.UserCriteria;
import com.project.libmanager.entity.User;
import com.project.libmanager.service.dto.request.UserCreateRequest;
import com.project.libmanager.service.dto.request.UserUpdateRequest;
import com.project.libmanager.service.dto.response.UserResponse;
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

    Page<UserResponse> searchUser(UserCriteria criteria, Pageable pageable);

    User findByEmail(String email);
}
