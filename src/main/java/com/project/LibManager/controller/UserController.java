package com.project.LibManager.controller;

import com.project.LibManager.criteria.UserCriteria;
import com.project.LibManager.dto.request.UserCreateRequest;
import com.project.LibManager.dto.request.UserUpdateRequest;
import com.project.LibManager.dto.response.ApiResponse;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.entity.User;
import com.project.LibManager.service.IMailService;
import com.project.LibManager.service.IUserService;
import com.project.LibManager.specification.UserQueryService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "JWT Authentication")
public class UserController {
    private final IUserService userService;
    private final IMailService mailService;
    private final UserQueryService userQueryService;


   @PostMapping("/admin")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody @Valid UserCreateRequest userCreateRequest) {
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .result(userService.createUser(userCreateRequest))
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(@RequestParam(defaultValue = "0") int offset,
                                            @RequestParam(defaultValue = "10") int limit) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("User: {}", authentication.getName());
        authentication.getAuthorities().forEach(gr -> log.info("Role: {}", gr.getAuthority()));
        
        Pageable pageable = PageRequest.of(offset, limit);
        ApiResponse<Page<UserResponse>> response = ApiResponse.<Page<UserResponse>>builder()
                .result(userService.getUsers(pageable))
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long userId) {
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .result(userService.getUser(userId))
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/info")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo() {
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@RequestBody @Valid UserUpdateRequest userUpdateRequest, @PathVariable Long id) {
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .message("Update user successfully")
                .result(userService.updateUser(id, userUpdateRequest))
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Delete user successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/search")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(UserCriteria criteria, Pageable pageable) {
        Page<User> users = userQueryService.findByCriteria(criteria, pageable);
        Page<UserResponse> usersResponse = userService.mapUserPageUserResponsePage(users);
        ApiResponse<Page<UserResponse>> response = ApiResponse.<Page<UserResponse>>builder()
                .message("Search user successfully")
                .result(usersResponse)
                .build();
        return ResponseEntity.ok().body(response);
    }
}
