package com.project.LibManager.controller;

import com.project.LibManager.dto.request.SearchUserRequest;
import com.project.LibManager.dto.request.UserCreateRequest;
import com.project.LibManager.dto.request.UserUpdateRequest;
import com.project.LibManager.dto.response.ApiResponse;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.service.MailService;
import com.project.LibManager.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "JWT Authentication")
public class UserController {
    UserService userService;
    MailService mailService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreateRequest userCreateRequest) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(userCreateRequest))
                .build();
    }
    @GetMapping
    ApiResponse<Page<UserResponse>> getUsers(@RequestParam(defaultValue = "0") int offset,
                                            @RequestParam(defaultValue = "10") int limit) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("User: {}", authentication.getName());
        authentication.getAuthorities().forEach(gr -> log.info("Role: {}", gr.getAuthority()));
        Pageable pageable = PageRequest.of(offset, limit);
        return ApiResponse.<Page<UserResponse>>builder()
                .result(userService.getUsers(pageable))
                .build();
    }
    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable Long userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUser(userId))
                .build();
    }

    @GetMapping("/email")
    ApiResponse<String> sendEmail(@RequestParam("fullName") String fullName, @RequestParam("token") String token, @RequestParam("email") String email) {
        mailService.sendEmailVerify(fullName, token, email);
        return ApiResponse.<String>builder()
                .result("Email sent successfully")
                .build();
    }

    @GetMapping("/info")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @PutMapping("{id}")
    ApiResponse<UserResponse> updateUser(@RequestBody @Valid UserUpdateRequest userUpdateRequest, @PathVariable Long id) {
        return ApiResponse.<UserResponse>builder()
                .message("Update user successfully")
                .result(userService.updateUser(id, userUpdateRequest))
                .build();
    }
    
    @DeleteMapping("{id}")
    ApiResponse<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.<String>builder()
                .message("Delete user successfully")
                .build();
    }

    @PostMapping("/search") 
    ApiResponse<Page<UserResponse>> searchUsers(@RequestBody @Valid SearchUserRequest searchUserRequest,
                                               @RequestParam(defaultValue = "0") int offset,
                                               @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return ApiResponse.<Page<UserResponse>>builder()
                .result(userService.searchUsers(searchUserRequest, pageable))
                .build();
    }
}
