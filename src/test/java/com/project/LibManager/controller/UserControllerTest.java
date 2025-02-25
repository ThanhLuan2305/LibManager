package com.project.LibManager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.LibManager.constant.ErrorCode;
import com.project.LibManager.dto.request.UserCreateRequest;
import com.project.LibManager.dto.request.UserUpdateRequest;
import com.project.LibManager.dto.response.RoleResponse;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.exception.AppException;
import com.project.LibManager.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.Collections;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IUserService userService;

    private UserCreateRequest createRequest;
    private UserResponse userResponse;
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
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_success() throws Exception {
        //GIVEN
        ObjectMapper object = new ObjectMapper();
        object.registerModule(new JavaTimeModule());
        String content = object.writeValueAsString(createRequest);

        Mockito.when(userService.createUser(ArgumentMatchers.any())).thenReturn(userResponse);

        //WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/admin")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(200)
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_invalid() throws Exception {
        //GIVEN
        createRequest.setEmail("ntll");
        ObjectMapper object = new ObjectMapper();
        object.registerModule(new JavaTimeModule());
        String content = object.writeValueAsString(createRequest);

        //WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users/admin")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(1003))
                .andExpect(MockMvcResultMatchers
                .jsonPath("message")
                .value("Invalid email address"));
        }

    @Test
    @WithMockUser(roles = "ADMIN")
        void getUsers_success() throws Exception {
        // Given
        int offset = 0;
        int limit = 10;
        Pageable pageable = PageRequest.of(offset, limit);
        Page<UserResponse> userPage = new PageImpl<>(Collections.emptyList());

        Mockito.when(userService.getUsers(pageable)).thenReturn(userPage);

        // When, Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/admin")
                        .param("offset", String.valueOf(offset))
                        .param("limit", String.valueOf(limit)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").exists());

        Mockito.verify(userService).getUsers(pageable);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsers_emptyResultSet() throws Exception {
        // Given
        int offset = 0;
        int limit = 10;
        Pageable pageable = PageRequest.of(offset, limit);
        Page<UserResponse> emptyPage = new PageImpl<>(Collections.emptyList());

        Mockito.when(userService.getUsers(pageable)).thenReturn(emptyPage);

        // When, Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/admin")
                        .param("offset", String.valueOf(offset))
                        .param("limit", String.valueOf(limit)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.content").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.content").isEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.totalElements").value(0));

        Mockito.verify(userService).getUsers(pageable);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUser_success() throws Exception {
        // Given
        Long userId = 1L;
        UserResponse mockUserResponse = UserResponse.builder()
                .id(userId)
                .email("test@example.com")
                .fullName("Test User")
                .isVerified(true)
                .roles(Collections.singleton(roleResponse))
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        Mockito.when(userService.getUser(userId)).thenReturn(mockUserResponse);

        // When, Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/admin/" + userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.id").value(userId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.email").value("test@example.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.fullName").value("Test User"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.isVerified").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.roles[0].name").value("USER"));

        Mockito.verify(userService).getUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUser_notFound() throws Exception {
        // Given
        Long nonExistentUserId = 999L;
        Mockito.when(userService.getUser(nonExistentUserId))
                .thenThrow(new AppException(ErrorCode.USER_NOT_EXISTED));

        // When, Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/admin/" + nonExistentUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(1005))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User not existed"));

        Mockito.verify(userService).getUser(nonExistentUserId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getMyInfo_success() throws Exception {
        // Given
        UserResponse mockUserResponse = UserResponse.builder()
                .id(1L)
                .email("user@example.com")
                .fullName("Test User")
                .isVerified(true)
                .roles(Collections.singleton(roleResponse))
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        Mockito.when(userService.getMyInfo()).thenReturn(mockUserResponse);

        // When, Then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/user/info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.email").value("user@example.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.fullName").value("Test User"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.isVerified").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.roles[0].name").value("USER"));

        Mockito.verify(userService).getMyInfo();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_success() throws Exception {
        // Given
        Long userId = 1L;
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .fullName("Updated Name")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        UserResponse updatedUserResponse = UserResponse.builder()
                .id(userId)
                .email("user@example.com")
                .fullName("Updated Name")
                .isVerified(true)
                .roles(Collections.singleton(roleResponse))
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(updateRequest);

        Mockito.when(userService.updateUser(ArgumentMatchers.eq(userId), ArgumentMatchers.any(UserUpdateRequest.class)))
                .thenReturn(updatedUserResponse);

        // When, Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/admin/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Update user successfully"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.id").value(userId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.fullName").value("Updated Name"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.birthDate").value("1990-01-01"));

        Mockito.verify(userService).updateUser(ArgumentMatchers.eq(userId), ArgumentMatchers.any(UserUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_invalidData_returnsBadRequest() throws Exception {
        // Given
        Long userId = 1L;
        UserUpdateRequest invalidUpdateRequest = UserUpdateRequest.builder()
                .fullName("")
                .birthDate(LocalDate.now().plusDays(1))
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(invalidUpdateRequest);

        // When, Then
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/users/admin/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(1022))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Birth date must be in past"));

        Mockito.verify(userService, Mockito.never()).updateUser(ArgumentMatchers.anyLong(), ArgumentMatchers.any(UserUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_success() throws Exception {
        // Given
        Long userId = 1L;

        // When, Then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/users/admin/" + userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Delete user successfully"));

        Mockito.verify(userService).deleteUser(userId);

        // Verify that the user is actually deleted
        Mockito.when(userService.getUser(userId)).thenThrow(new AppException(ErrorCode.USER_NOT_EXISTED));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/admin/" + userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(1005))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User not existed"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_nonExistentUser_returnsNotFound() throws Exception {
        // Given
        Long nonExistentUserId = 999L;
        Mockito.doThrow(new AppException(ErrorCode.USER_NOT_EXISTED))
                .when(userService).deleteUser(nonExistentUserId);

        // When, Then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/users/admin/" + nonExistentUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(1005))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User not existed"));

        Mockito.verify(userService).deleteUser(nonExistentUserId);
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_nullId_returnsBadRequest() throws Exception {
        // Given
        String nullId = null;

        // When, Then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/users/admin/" + nullId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(9999))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Method parameter 'id': Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; For input string: \"null\""));

        Mockito.verify(userService, Mockito.never()).deleteUser(Mockito.any());
    }

}
