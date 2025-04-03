package com.project.libmanager.controller.admin;

import com.project.libmanager.criteria.UserCriteria;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.service.IUserService;
import com.project.libmanager.service.dto.request.UserCreateRequest;
import com.project.libmanager.service.dto.request.UserUpdateRequest;
import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * REST controller for managing user operations by admin users.
 * Provides endpoints for creating, updating, deleting, retrieving, and searching users.
 */
@RestController
@RequestMapping("admin/users")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "JWT Authentication")
@Tag(name = "Admin User Management", description = "Endpoints for managing users by admin users")
public class AdminUserController {
    private final IUserService userService;

    /**
     * Creates a new user with the provided details.
     *
     * @param userCreateRequest the request containing user creation details
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a {@link UserResponse} detailing the created user
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - user not authorized (ErrorCode.UNAUTHORIZED)
     *                      - email or phone already exists (ErrorCode.USER_EXISTED)
     * @implNote Delegates to {@link IUserService} to create the user and returns the user details in an {@link ApiResponse}.
     */
    @PostMapping
    @Operation(summary = "Create a new user",
            description = "Creates a new user with the provided details.")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @RequestBody @Valid UserCreateRequest userCreateRequest) {
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .result(userService.createUser(userCreateRequest))
                .message("User created successfully.")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing user with the provided details.
     *
     * @param userUpdateRequest the request containing user update details
     * @param id                the ID of the user to update
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a {@link UserResponse} detailing the updated user
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - user not authorized (ErrorCode.UNAUTHORIZED)
     *                      - user not found (ErrorCode.USER_NOT_EXISTED)
     * @implNote Delegates to {@link IUserService} to update the user and returns the updated user details in an {@link ApiResponse}.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a user",
            description = "Updates an existing user with the provided details.")
    @Parameter(name = "id", description = "ID of the user to update")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @RequestBody @Valid UserUpdateRequest userUpdateRequest,
            @PathVariable Long id) {
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .message("User updated successfully.")
                .result(userService.updateUser(id, userUpdateRequest))
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id the ID of the user to delete
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a success message
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - user not authorized (ErrorCode.UNAUTHORIZED)
     *                      - user not found (ErrorCode.USER_NOT_EXISTED)
     * @implNote Delegates to {@link IUserService} to delete the user and returns a success message in an {@link ApiResponse}.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user",
            description = "Deletes a user by their ID.")
    @Parameter(description = "ID of the user to delete")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("User deleted successfully.")
                .result("success")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Searches for users based on specified criteria with pagination.
     *
     * @param criteria the search criteria for filtering users
     * @param pageable the pagination information
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a {@link Page} of {@link UserResponse} objects
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - user not authorized (ErrorCode.UNAUTHORIZED)
     *                      - invalid search criteria (ErrorCode.INVALID_INPUT)
     * @implNote Uses {@link UserCriteria} and {@link Pageable} to search users via {@link IUserService} and wraps the results in an {@link ApiResponse}.
     */
    @GetMapping("/search")
    @Operation(summary = "Search users",
            description = "Searches for users based on specified criteria with pagination.")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> serachUsers(@ParameterObject UserCriteria criteria,
                                                                       @ParameterObject Pageable pageable) {
        ApiResponse<Page<UserResponse>> response = ApiResponse.<Page<UserResponse>>builder()
                .message("Users retrieved successfully based on search criteria.")
                .result(userService.searchUser(criteria, pageable))
                .build();
        return ResponseEntity.ok().body(response);
    }

    /**
     * Retrieves a paginated list of all users.
     *
     * @param offset the page number (starting from 0)
     * @param limit  the number of items per page
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a {@link Page} of {@link UserResponse} objects
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - user not authorized (ErrorCode.UNAUTHORIZED)
     *                      - invalid pagination parameters (ErrorCode.INVALID_INPUT)
     * @implNote Uses {@link Pageable} to fetch users from {@link IUserService} and wraps them in an {@link ApiResponse}.
     */
    @GetMapping
    @Operation(summary = "Get all users",
            description = "Retrieves a paginated list of all users.")
    @Parameter(name = "offset", description = "Page number (default: 0)")
    @Parameter(name = "limit", description = "Items per page (default: 10)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(@RequestParam(defaultValue = "0") int offset,
                                                                    @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        ApiResponse<Page<UserResponse>> response = ApiResponse.<Page<UserResponse>>builder()
                .message("Users retrieved successfully.")
                .result(userService.getUsers(pageable))
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves details of a specific user by their ID.
     *
     * @param userId the ID of the user to retrieve
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a {@link UserResponse} detailing the user
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - user not authorized (ErrorCode.UNAUTHORIZED)
     *                      - user not found (ErrorCode.USER_NOT_EXISTED)
     * @implNote Fetches the user from {@link IUserService} and returns the details in an {@link ApiResponse}.
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get a user by ID",
            description = "Retrieves details of a specific user by their ID.")
    @Parameter(description = "ID of the user to retrieve")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long userId) {
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .message("User retrieved successfully.")
                .result(userService.getUser(userId))
                .build();
        return ResponseEntity.ok(response);
    }
}