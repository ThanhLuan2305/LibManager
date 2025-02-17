package com.project.LibManager.service.impl;

import com.project.LibManager.constant.ErrorCode;
import com.project.LibManager.constant.PredefinedRole;
import com.project.LibManager.dto.request.SearchUserRequest;
import com.project.LibManager.dto.request.UserCreateRequest;
import com.project.LibManager.dto.request.UserUpdateRequest;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.entity.Role;
import com.project.LibManager.entity.User;
import com.project.LibManager.exception.AppException;
import com.project.LibManager.mapper.UserMapper;
import com.project.LibManager.repository.RoleRepository;
import com.project.LibManager.repository.UserRepository;
import com.project.LibManager.service.IUserService;
import com.project.LibManager.specification.UserSpecification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public UserResponse createUser(UserCreateRequest request) {
        User user = userMapper.toUser(request);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if(userRepository.existsByEmail(request.getEmail())) 
        	throw new AppException(ErrorCode.USER_EXISTED);

        Role role = roleRepository.findByName(PredefinedRole.USER_ROLE).orElseThrow(() -> 
            new AppException(ErrorCode.ROLE_NOT_EXISTED));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        user.setIsVerified(false);
        user.setIsDeleted(false);
        try {
            userRepository.save(user);
            return userMapper.toUserResponse(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
    @Override
    public Page<UserResponse> getUsers(Pageable pageable) {
        return mapUserPageUserResponsePage(userRepository.findAll(pageable));
    }
    @Override
    public Page<UserResponse> mapUserPageUserResponsePage(Page<User> userPage) {
        List<UserResponse> userResponses = userPage.getContent().stream()
                                            .map(user -> mapToUserResponseByMapper(user.getId()))
                                            .collect(Collectors.toList());
    
            return new PageImpl<>(userResponses, userPage.getPageable(), userPage.getTotalElements());
    }
    @Override
    public UserResponse mapToUserResponseByMapper(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (user == null) {
            return null;
        }
        return userMapper.toUserResponse(user);
    }
    @Override
    public UserResponse getUser(Long id) {
        return userMapper.toUserResponse(
                            userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }
    @Override
    public UserResponse getMyInfo() {
        try {
            var jwtContext = SecurityContextHolder.getContext();
    
            if (jwtContext == null || jwtContext.getAuthentication() == null || 
                !jwtContext.getAuthentication().isAuthenticated()) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
    
            String email = jwtContext.getAuthentication().getName();
            User u = userRepository.findByEmail(email);
    
            if (u == null) {
                throw new AppException(ErrorCode.USER_NOT_EXISTED);
            }
    
            return userMapper.toUserResponse(u);
        } catch (AppException e) {
            log.error("Error getting user info: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in getMyInfo: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Transactional
    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User u = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (!request.getEmail().equals(u.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        try {

            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                u.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            userMapper.updateUser(u, request);

            var requestedRoles = new HashSet<>(roleRepository.findByNameIn(request.getRoles()));

            requestedRoles.removeIf(role -> role.getName().equalsIgnoreCase("ADMIN"));

            if (requestedRoles.stream().anyMatch(role -> role.getName().equalsIgnoreCase("ADMIN"))) {
                requestedRoles.stream()
                    .filter(role -> role.getName().equalsIgnoreCase("ADMIN"))
                    .forEach(requestedRoles::add);
            }
            u.setRoles(requestedRoles);

            userRepository.save(u);
            return userMapper.toUserResponse(u);
        } catch (Exception e) {
            log.error("Error updating user: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        try {
            if (!user.getBorrowings().isEmpty()) {
                user.setIsDeleted(true);
                userRepository.save(user);
            } else {
                user.getRoles().clear();
                userRepository.save(user);
                userRepository.delete(user);
            }
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public Page<UserResponse> searchUsers(SearchUserRequest SearchUserRequest, Pageable pageable) {
        try {
            return mapUserPageUserResponsePage(userRepository.findAll(UserSpecification.filterUsers(SearchUserRequest.getFullName(), SearchUserRequest.getEmail(), SearchUserRequest.getRole(), SearchUserRequest.getFromDate(), SearchUserRequest.getToDate() ), pageable));
        } catch (Exception e) {
            log.error("Error serching user: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}
