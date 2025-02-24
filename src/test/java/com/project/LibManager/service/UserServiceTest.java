package com.project.LibManager.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import com.project.LibManager.constant.PredefinedRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.project.LibManager.constant.ErrorCode;
import com.project.LibManager.dto.request.SearchUserRequest;
import com.project.LibManager.dto.request.UserCreateRequest;
import com.project.LibManager.dto.request.UserUpdateRequest;
import com.project.LibManager.dto.response.RoleResponse;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.entity.Borrowing;
import com.project.LibManager.entity.Role;
import com.project.LibManager.entity.User;
import com.project.LibManager.exception.AppException;
import com.project.LibManager.mapper.UserMapper;
import com.project.LibManager.repository.RoleRepository;
import com.project.LibManager.repository.UserRepository;
import com.project.LibManager.service.impl.UserServiceImpl;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@TestPropertySource("/test.properties")
public class UserServiceTest {
    @Autowired
    private UserServiceImpl userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private UserCreateRequest createRequest;
    private UserResponse userResponse;
    private UserUpdateRequest userUpdateRequest;
    private SearchUserRequest searchUserRequest;
    private Role role;
    private User user;
    private RoleResponse roleResponse;
    private LocalDate birthDate;

    @BeforeEach
    void initData() {
        birthDate = LocalDate.of(2001, 1, 1);
        createRequest = UserCreateRequest.builder()
                .email("lta@gmail.com")
                .fullName("Le Trong An")
                .password("123123")
                .birthDate(birthDate)
                .build();

        roleResponse = RoleResponse.builder()
                .id(4L)
                .name("USER")
                .description("User role")
                .build();

        userResponse = UserResponse.builder()
                .id(16L)
                .email("lta@gmail.com")
                .fullName("Le Trong An")
                .isVerified(false)
                .roles(Collections.singleton(roleResponse))
                .birthDate(birthDate)
                .build();

        role = Role.builder()
                .id(4L)
                .name("USER")
                .description("User role")
                .build();
        
        user = User.builder()
                .id(16L)
                .email("lta@gmail.com")
                .fullName("Le Trong An")
                .isVerified(false)
                .birthDate(birthDate)
                .password("123123")
                .isDeleted(false)
                .build();

        userUpdateRequest = UserUpdateRequest.builder()
                .email("testuser@example.com")
                .password("securePassword123")
                .isVerified(true)
                .fullName("Test User")
                .birthDate(birthDate)
                .build();

        searchUserRequest = SearchUserRequest.builder()
                .fullName("Test User")
                .email("testuser@example.com")
                .role("USER")
                .fromDate(LocalDate.of(2023, 1, 1))
                .toDate(LocalDate.of(2024, 1, 1))
                .build();
    }

    @Test
    void createUser_success() {
        //GIVEN
        when(userMapper.toUser(createRequest)).thenReturn(user);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(java.util.Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        //WHEN
        UserResponse result = userService.createUser(createRequest);

        //THEN
        verify(userMapper).toUser(createRequest);
        verify(userRepository).existsByEmail(createRequest.getEmail());
        verify(roleRepository).findByName("USER");
        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserResponse(user);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getEmail()).isEqualTo(createRequest.getEmail());
        Assertions.assertThat(result.getFullName()).isEqualTo(createRequest.getFullName());
        Assertions.assertThat(result.getBirthDate()).isEqualTo(createRequest.getBirthDate());
        Assertions.assertThat(result.getIsVerified()).isFalse();
        Assertions.assertThat(result.getRoles()).hasSize(1);
        Assertions.assertThat(result.getRoles().iterator().next().getName()).isEqualTo("USER");
    }

    @Test
    void createUser_emailExisted_fail() {
        // GIVEN
        UserCreateRequest request = UserCreateRequest.builder()
                .email("existing@example.com")
                .fullName("Test User")
                .password("password123")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        User mockUser = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .birthDate(request.getBirthDate())
                .build();

        when(userMapper.toUser(request)).thenReturn(mockUser);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // WHEN
        AppException exception = assertThrows(AppException.class, () -> userService.createUser(request));

        // THEN
        Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_EXISTED);
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userMapper).toUser(request);
        verifyNoMoreInteractions(userRepository, roleRepository, userMapper);
    }

    @Test
    void createUser_roleNotFound_fail() {
        // GIVEN
        UserCreateRequest request = UserCreateRequest.builder()
                .email("test@example.com")
                .fullName("Test User")
                .password("password123")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        User mockUser = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .birthDate(request.getBirthDate())
                .build();

        when(userMapper.toUser(request)).thenReturn(mockUser);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(PredefinedRole.USER_ROLE)).thenReturn(Optional.empty());

        // WHEN
        AppException exception = assertThrows(AppException.class, () -> userService.createUser(request));

        // THEN
        Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ROLE_NOT_EXISTED);
        verify(userRepository).existsByEmail(request.getEmail());
        verify(roleRepository).findByName(PredefinedRole.USER_ROLE);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getUsers_userNotExisted_throwsAppException() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Collections.singletonList(user));
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toUserResponse(any(User.class))).thenThrow(new AppException(ErrorCode.USER_NOT_EXISTED));

        // WHEN & THEN
        AppException exception = assertThrows(AppException.class, () -> userService.getUsers(pageable));
        Assertions.assertThat(exception.getMessage()).isEqualTo("User not existed");
        Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_EXISTED);
    }

    @Test
    void getUsers_pageNotEmpty_success() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Collections.singletonList(user));
        
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        // WHEN
        Page<UserResponse> result = userService.getUsers(pageable);

        // THEN
        verify(userRepository).findAll(pageable);
        verify(userRepository).findById(user.getId());
        verify(userMapper).toUserResponse(user);
        
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getContent()).hasSize(1);
        Assertions.assertThat(result.getContent().get(0)).isEqualTo(userResponse);
    }


    @Test
    void getUsers_Fail_EmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(Page.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> userService.getUsers(pageable));

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void getUser_success() {
        // GIVEN
        Long userId = 16L;
        User mockUser = User.builder()
                .id(userId)
                .email("lta@gmail.com")
                .fullName("Le Trong An")
                .isVerified(false)
                .birthDate(birthDate)
                .build();
        UserResponse expectedResponse = UserResponse.builder()
                .id(userId)
                .email("lta@gmail.com")
                .fullName("Le Trong An")
                .isVerified(false)
                .birthDate(birthDate)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userMapper.toUserResponse(mockUser)).thenReturn(expectedResponse);

        // WHEN
        UserResponse result = userService.getUser(userId);

        // THEN
        verify(userRepository).findById(userId);
        verify(userMapper).toUserResponse(mockUser);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(userId);
        Assertions.assertThat(result.getEmail()).isEqualTo("lta@gmail.com");
        Assertions.assertThat(result.getFullName()).isEqualTo("Le Trong An");
        Assertions.assertThat(result.getIsVerified()).isFalse();
        Assertions.assertThat(result.getBirthDate()).isEqualTo(birthDate);
    }
    
    @Test
    void getUser_userNotFound_throwsAppException() {
        // GIVEN
        Long nonExistentUserId = 999L;
        when(userRepository.findById(nonExistentUserId)).thenReturn(java.util.Optional.empty());

        // WHEN & THEN
        AppException exception = assertThrows(AppException.class, () -> userService.getUser(nonExistentUserId));
        Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_EXISTED);
    }

    @Test
    void getMyInfo_authenticationIsNull_throwsUnauthorizedException() {
        // GIVEN
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // WHEN & THEN
        AppException exception = assertThrows(AppException.class, () -> userService.getMyInfo());
        Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    void getMyInfo_userEmailNotFound_throwsUserNotExistedException() {
        // GIVEN
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("nonexistent@example.com");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(null);

        // WHEN & THEN
        AppException exception = assertThrows(AppException.class, () -> userService.getMyInfo());
        Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_EXISTED);
    }

    @Test
    void getMyInfo_unexpectedError_throwsUncategorizedException() {
        // GIVEN
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("test@example.com")).thenThrow(new RuntimeException("Unexpected error"));

        // WHEN & THEN
        AppException exception = assertThrows(AppException.class, () -> userService.getMyInfo());
        Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNCATEGORIZED_EXCEPTION);
    }

    @Test
    void updateUser_emailAlreadyExists_throwsAppException() {
        // GIVEN
        Long userId = 1L;
        String existingEmail = "existing@example.com";
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email(existingEmail)
                .fullName("Updated Name")
                .build();

        User existingUser = User.builder()
                .id(userId)
                .email("original@example.com")
                .fullName("Original Name")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(existingEmail)).thenReturn(true);

        // WHEN & THEN
        AppException exception = assertThrows(AppException.class, () -> userService.updateUser(userId, request));
        Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_EXISTED);
    }

    @Test
    void updateUser_nonExistentUser_throwsAppException() {
        // GIVEN
        Long nonExistentUserId = 999L;
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .email("newmail@example.com")
                .fullName("Updated Name")
                .build();
        when(userRepository.findById(nonExistentUserId)).thenReturn(java.util.Optional.empty());

        // WHEN & THEN
        AppException exception = assertThrows(AppException.class, () -> userService.updateUser(nonExistentUserId, updateRequest));
        Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_EXISTED);
    }

    @Test
    void updateUser_success() {
        // GIVEN
        Long userId = 16L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(userUpdateRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);

        // WHEN
        UserResponse result = userService.updateUser(userId, userUpdateRequest);

        // THEN
        Assertions.assertThat(result.getId()).isEqualByComparingTo(16L);
        Assertions.assertThat(result.getEmail()).isEqualTo("lta@gmail.com");
        verify(userMapper).updateUser(eq(user), eq(userUpdateRequest));
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_unexpectedError_throwsUncategorizedAppException() {
        // GIVEN
        Long userId = 1L;
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("test@example.com")
                .password("newPassword")
                .build();
        User existingUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        doThrow(new RuntimeException("Unexpected error")).when(userMapper).updateUser(any(), any());

        // WHEN & THEN
        AppException exception = assertThrows(AppException.class, () -> userService.updateUser(userId, request));
        Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNCATEGORIZED_EXCEPTION);
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void deleteUser_withBorrowings_marksUserAsDeleted() {
        // GIVEN
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .isDeleted(false)
                .borrowings(new HashSet<>(Collections.singletonList(new Borrowing())))
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // WHEN
        userService.deleteUser(userId);

        // THEN
        Assertions.assertThat(user.getIsDeleted()).isTrue();
        verify(userRepository, times(1)).save(user);
        verify(userRepository, never()).delete(user);
    }

    @Test
    void deleteUser_userWithNoBorrowings_fullyDeletesUser() {
        // GIVEN
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .fullName("Test User")
                .borrowings(new HashSet<>())
                .roles(new HashSet<>(Collections.singletonList(new Role())))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // WHEN
        userService.deleteUser(userId);

        // THEN
        Assertions.assertThat(user.getRoles()).isEmpty();
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
        verify(userRepository).delete(user);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUser_unexpectedError_throwsUncategorizedException() {
        // GIVEN
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("Unexpected error")).when(userRepository).save(any(User.class));

        // WHEN & THEN
        AppException exception = assertThrows(AppException.class, () -> userService.deleteUser(userId));
        Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.UNCATEGORIZED_EXCEPTION);
    }

    
}
