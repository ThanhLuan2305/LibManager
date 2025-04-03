package com.project.libmanager.controller.common;

import com.project.libmanager.exception.AppException;
import com.project.libmanager.service.IPasswordService;
import com.project.libmanager.service.dto.request.ResetPasswordRequest;
import com.project.libmanager.service.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * REST controller for managing password-related operations.
 * Provides endpoints for requesting a password reset and resetting the password.
 */
@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
@Tag(name = "Password Management", description = "Endpoints for password reset operations")
public class PasswordController {
    private final IPasswordService passwordService;

    /**
     * Initiates a password reset process by sending a reset link to the user's email.
     *
     * @param email the email address of the user requesting a password reset
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a success message
     * @throws AppException if:
     *                      - email not found (ErrorCode.USER_NOT_EXISTED)
     *                      - email sending fails (ErrorCode.EMAIL_SENDING_FAILED)
     * @implNote Delegates to {@link IPasswordService} to send a reset link and returns a success message in an {@link ApiResponse}.
     */
    @PutMapping("/forget-password")
    @Operation(summary = "Request password reset",
            description = "Initiates a password reset process by sending a reset link to the user's email.")
    @Parameter(name = "email", description = "Email address of the user")
    public ResponseEntity<ApiResponse<String>> forgetPassword(@RequestParam("email") String email) {
        passwordService.forgetPassword(email);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Please check your email to reset password")
                .result("success")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Resets the user's password using a reset token and new password details.
     *
     * @param resetPasswordRequest the request body containing the reset token and new password details
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a success message
     * @throws AppException if:
     *                      - token is invalid or expired (ErrorCode.INVALID_TOKEN)
     *                      - password validation fails (ErrorCode.INVALID_PASSWORD)
     * @implNote Delegates to {@link IPasswordService} to reset the password and returns a success message in an {@link ApiResponse}.
     */
    @PutMapping("/reset-password")
    @Operation(summary = "Reset password",
            description = "Resets the user's password using a reset token and new password details.")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        passwordService.resetPassword(resetPasswordRequest);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Reset password successfully, you can login with new password")
                .result("success")
                .build();
        return ResponseEntity.ok(response);
    }
}