package com.project.LibManager.service;

import com.project.LibManager.constant.PredefinedRole;
import com.project.LibManager.dto.request.SearchUserRequest;
import com.project.LibManager.dto.request.UserCreateRequest;
import com.project.LibManager.dto.request.UserUpdateRequest;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.entity.Role;
import com.project.LibManager.entity.User;
import com.project.LibManager.exception.AppException;
import com.project.LibManager.exception.ErrorCode;
import com.project.LibManager.mapper.UserMapper;
import com.project.LibManager.repository.RoleRepository;
import com.project.LibManager.repository.UserRepository;
import com.project.LibManager.specification.UserSpecification;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createUser(UserCreateRequest request) {
        User user = userMapper.toUser(request);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if(userRepository.existsByEmail(request.getEmail())) 
        	throw new AppException(ErrorCode.USER_EXISTED);

        Role role = roleRepository.findById(PredefinedRole.USER_ROLE).orElseThrow(() -> 
            new AppException(ErrorCode.ROLE_NOT_EXISTED));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        user.setIsVerified(false);
        
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        return userMapper.toUserResponse(user);
    }
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponse> getUsers(Pageable pageable) {
        return mapUserPageUserResponsePage(userRepository.findAll(pageable));
    }
    public Page<UserResponse> mapUserPageUserResponsePage(Page<User> userPage) {
        List<UserResponse> userResponses = userPage.getContent().stream()
            .map(user -> mapToUserResponseByMapper(user.getId()))
            .collect(Collectors.toList());

        return new PageImpl<>(userResponses, userPage.getPageable(), userPage.getTotalElements());
	}
    public UserResponse mapToUserResponseByMapper(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (user == null) {
            return null;
        }
        return userMapper.toUserResponse(user);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(Long id) {
        return userMapper.toUserResponse(
                    userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    public UserResponse getMyInfo() {
        var jwtContex = SecurityContextHolder.getContext();

        User u = userRepository.findByEmail(jwtContex.getAuthentication().getName()); 
        if(u == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);

        return userMapper.toUserResponse(u);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        try {
            User u = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            if( userRepository.existsByEmail(request.getEmail())) 
                throw new AppException(ErrorCode.USER_EXISTED);
            userMapper.updateUser(u, request);
            u.setPassword(passwordEncoder.encode(request.getPassword()));

            var roles = roleRepository.findAllById(request.getRoles());
            u.setRoles(new HashSet<>(roles));

            userRepository.save(u);
            return userMapper.toUserResponse(u);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Set<Role> roles = user.getRoles();
        roles.clear();
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Page<UserResponse> searchUsers(SearchUserRequest SearchUserRequest, Pageable pageable) {
        try {
            return mapUserPageUserResponsePage(userRepository.findAll(UserSpecification.filterUsers(SearchUserRequest.getFullName(), SearchUserRequest.getEmail(), SearchUserRequest.getRole(), SearchUserRequest.getFromDate(), SearchUserRequest.getToDate() ), pageable));
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}
