package com.project.LibManager.controller.admin;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.LibManager.criteria.UserCriteria;
import com.project.LibManager.service.dto.request.UserCreateRequest;
import com.project.LibManager.service.dto.request.UserUpdateRequest;
import com.project.LibManager.service.dto.response.ApiResponse;
import com.project.LibManager.service.dto.response.UserResponse;
import com.project.LibManager.service.IUserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("admin/users")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "JWT Authentication")
public class AdminUserController {
    private final IUserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @RequestBody @Valid UserCreateRequest userCreateRequest) {
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .result(userService.createUser(userCreateRequest))
                .message("User created successfully.")
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @RequestBody @Valid UserUpdateRequest userUpdateRequest,
            @PathVariable Long id) {
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .message("User updated successfully.")
                .result(userService.updateUser(id, userUpdateRequest))
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("User deleted successfully.")
                .result("success")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> serachUsers(@ParameterObject UserCriteria criteria,
            Pageable pageable) {

        ApiResponse<Page<UserResponse>> response = ApiResponse.<Page<UserResponse>>builder()
                .message("Users retrieved successfully based on search criteria.")
                .result(userService.searchUSer(criteria, pageable))
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(@RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        Pageable pageable = PageRequest.of(offset, limit);
        ApiResponse<Page<UserResponse>> response = ApiResponse.<Page<UserResponse>>builder()
                .message("Users retrieved successfully.")
                .result(userService.getUsers(pageable))
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long userId) {
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .message("User retrieved successfully.")
                .result(userService.getUser(userId))
                .build();
        return ResponseEntity.ok(response);
    }
}
