package com.project.libmanager.controller.user;

import com.project.libmanager.exception.AppException;
import com.project.libmanager.service.IAccountService;
import com.project.libmanager.service.IPasswordService;
import com.project.libmanager.service.IUserService;
import com.project.libmanager.service.dto.request.ChangeMailRequest;
import com.project.libmanager.service.dto.request.ChangePasswordRequest;
import com.project.libmanager.service.dto.request.ChangePhoneRequest;
import com.project.libmanager.service.dto.request.VerifyChangeMailRequest;
import com.project.libmanager.service.dto.request.VerifyChangePhoneRequest;
import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * REST controller for managing authenticated user account operations.
 * Provides endpoints for retrieving user information and updating account details such as password,
 */
@Tag(name = "User Account Management", description = "Authenticated user account management endpoints")
@RestController
@RequestMapping("user/users")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "JWT Authentication")
public class UserController {
    private final IUserService userService;         // Service for user data operations
    private final IAccountService accountService;   // Service for account management (fixed typo "acount" to "account")
    private final IPasswordService passwordService; // Service for password operations
    private static final String RS_SUCCESS = "success"; // Constant indicating successful operation result

    /**
     * Retrieves information about the currently authenticated user.
     *
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} with the user's {@link UserResponse}
     * @throws AppException if:
     *                      - user is not authenticated
     *                      - user not found
     */
    @Operation(summary = "Get user information", description = "Returns detailed information of the authenticated user.")
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo() {
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .message("Get info successfully!")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the password of the currently authenticated user.
     *
     * @param cpRequest the request body containing old and new password details
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} with a success indicator
     * @throws AppException if:
     *                      - authentication fails
     *                      - old password is incorrect
     *                      - validation fails
     */
    @Operation(summary = "Change user password", description = "Updates the password for the authenticated user.")
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Boolean>> changePassword(@Valid @RequestBody ChangePasswordRequest cpRequest) {
        Boolean result = passwordService.changePassword(cpRequest);
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .message("Change password successfully")
                .result(result)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Initiates the process of changing the email address for the authenticated user.
     *
     * @param emailRequest the request body containing old and new email details
     * @return {@link ResponseEntity} containing an {@link ApiResponse} with a success indicator
     * @throws AppException if:
     *                      - authentication fails
     *                      - email mismatch
     *                      - email already exists
     */
    @Operation(summary = "Initiate email change", description = "Requests an email change and sends a verification OTP.")
    @PutMapping("/change-mail")
    public ResponseEntity<ApiResponse<String>> changeMail(@Valid @RequestBody ChangeMailRequest emailRequest) {
        accountService.changeEmail(emailRequest);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Please verify your new email to complete the email change process")
                .result(RS_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Verifies an email change request using an OTP and updates the user's email.
     *
     * @param emailRequest the request body containing OTP and email details
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} with a success indicator
     * @throws AppException if:
     *                      - authentication fails
     *                      - OTP is invalid
     *                      - database error occurs
     */
    @Operation(summary = "Verify email change", description = "Verifies email change with OTP and updates the new email.")
    @PutMapping("/verify-change-mail")
    public ResponseEntity<ApiResponse<String>> verifyChangeMail(@Valid @RequestBody VerifyChangeMailRequest emailRequest) {
        accountService.verifyChangeEmail(emailRequest);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Change email successfully, you can login with new email")
                .result(RS_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Initiates a phone number change for the currently authenticated user.
     *
     * @param phoneRequest the request body containing old and new phone details
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} with a success indicator
     * @throws AppException if:
     *                      - authentication fails,
     *                      - phone mismatch,
     *                      - phone already exists
     */
    @Operation(summary = "Initiate phone change", description = "Requests a phone number change and sends a verification OTP.")
    @PutMapping("/change-phone")
    public ResponseEntity<ApiResponse<String>> changePhone(@Valid @RequestBody ChangePhoneRequest phoneRequest) {
        accountService.changePhone(phoneRequest);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Please verify your new phone to change phone")
                .result(RS_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Verifies a phone number change request using an OTP and updates the user's phone.
     *
     * @param phoneRequest the request body containing OTP and phone details
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} with a success indicator
     * @throws AppException if:
     *                      - authentication fails
     *                      - OTP is invalid
     *                      - phone mismatch
     */
    @Operation(summary = "Verify phone change", description = "Verifies phone number change with OTP and updates the new phone.")
    @PutMapping("/verify-change-phone")
    public ResponseEntity<ApiResponse<String>> verifyChangePhone(@Valid @RequestBody VerifyChangePhoneRequest phoneRequest) {
        accountService.verifyChangePhone(phoneRequest);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Change phone successfully, you can login with new phone")
                .result(RS_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }
}