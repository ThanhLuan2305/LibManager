package com.project.LibManager.service;

import com.project.LibManager.dto.request.UserCreateRequest;
import com.project.LibManager.dto.request.UserUpdateRequest;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface IUserService {

    public UserResponse createUser(UserCreateRequest request);
    
    public Page<UserResponse> getUsers(Pageable pageable);

    public Page<UserResponse> mapUserPageUserResponsePage(Page<User> userPage);

    public UserResponse mapToUserResponseByMapper(Long id);
    
    public UserResponse getUser(Long id);

    public UserResponse getMyInfo();

    public UserResponse updateUser(Long id, UserUpdateRequest request);

    public void deleteUser(Long userId);
}
