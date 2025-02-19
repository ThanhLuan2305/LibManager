package com.project.LibManager.service;

import java.time.LocalDate;
import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.project.LibManager.dto.request.UserCreateRequest;
import com.project.LibManager.dto.response.RoleResponse;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.entity.Role;
import com.project.LibManager.entity.User;
import com.project.LibManager.exception.AppException;
import com.project.LibManager.repository.RoleRepository;
import com.project.LibManager.repository.UserRepository;
import com.project.LibManager.service.impl.UserServiceImpl;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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

    private UserCreateRequest createRequest;
    private UserResponse userResponse;
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
                .isDeleted(false)
                .build();
    }

    @Test
    void createUser_success() {
        //GIVEN
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(java.util.Optional.of(role));
        when(userRepository.save(any())).thenReturn(user);

        //WHEN
        var rs = userService.createUser(createRequest);

        //THEN
        Assertions.assertThat(userResponse.getId()).isEqualByComparingTo(16L); 
        Assertions.assertThat(userResponse.getEmail()).isEqualTo("lta@gmail.com");
    }

    @Test
    void createUser_emailExisted_fail() {
        //GIVEN
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // WHEN
        var exception = assertThrows(AppException.class, () -> userService.createUser(createRequest));

        // THEN
        Assertions.assertThat(exception.getErrorCode().getCode()).isEqualTo(1002);
    }

    @Test
    void createUser_roleNotFound_fail() {
        //GIVEN
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(roleRepository.findByName("USER")).thenReturn(java.util.Optional.empty());
        // WHEN
        var exception = assertThrows(AppException.class, () -> userService.createUser(createRequest));

        // THEN
        Assertions.assertThat(exception.getErrorCode().getCode()).isEqualTo(1008);
    }
}
