package com.project.libmanager.controller.common;

import com.project.libmanager.exception.AppException;
import com.project.libmanager.service.IAccountService;
import com.project.libmanager.service.dto.request.RegisterRequest;
import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * REST controller for managing user account operations.
 * Provides endpoints for user registration, email/phone verification, and retrieving user roles.
 */
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
@Tag(name = "Account Management", description = "Endpoints for user registration and verification")
public class AccountController {
    private final IAccountService accountService;

    /**
     * Registers a new user with the provided details.
     *
     * @param registerRequest the request containing user registration details
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a {@link UserResponse} detailing the registered user
     * @throws AppException if:
     *                      - email or phone already exists (ErrorCode.USER_EXISTED)
     *                      - invalid registration data (ErrorCode.INVALID_INPUT)
     * @implNote Delegates registration to {@link IAccountService} and returns the user details in an {@link ApiResponse}.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user",
            description = "Registers a new user with the provided details.")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody @Valid RegisterRequest registerRequest) {
        UserResponse result = accountService.registerUser(registerRequest);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .result(result)
                .message("Register successfully, please verify your email and phone to login!")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Verifies the user's email using an OTP.
     *
     * @param otp   the one-time password for email verification
     * @param email the email address to verify
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a boolean indicating verification success
     * @throws AppException if:
     *                      - invalid or expired OTP (ErrorCode.INVALID_OTP)
     *                      - email not found (ErrorCode.USER_NOT_EXISTED)
     * @implNote Delegates email verification to {@link IAccountService} and returns the result in an {@link ApiResponse}.
     */
    @GetMapping("/verify-email")
    @Operation(summary = "Verify email",
            description = "Verifies the user's email using an OTP.")
    @Parameter(name = "otp", description = "One-time password for email verification")
    @Parameter(name = "email", description = "Email address to verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyEmail(@RequestParam("otp") String otp, @RequestParam("email") String email) {
        Boolean result = accountService.verifyEmail(otp, email);
        String message = result.booleanValue() ? "Email verification successful."
                : "Email verification failed.";
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .result(result)
                .message(message)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Verifies the user's phone number using an OTP.
     *
     * @param otp   the one-time password for phone verification
     * @param phone the phone number to verify
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a boolean indicating verification success
     * @throws AppException if:
     *                      - invalid or expired OTP (ErrorCode.INVALID_OTP)
     *                      - phone number not found (ErrorCode.USER_NOT_EXISTED)
     * @implNote Delegates phone verification to {@link IAccountService} and returns the result in an {@link ApiResponse}.
     */
    @PostMapping("/verify-phone")
    @Operation(summary = "Verify phone",
            description = "Verifies the user's phone number using an OTP.")
    @Parameter(name = "otp", description = "One-time password for phone verification")
    @Parameter(name = "phone", description = "Phone number to verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyPhone(@RequestParam("otp") String otp, @RequestParam("phone") String phone) {
        Boolean result = accountService.verifyPhone(otp, phone);
        String message = result.booleanValue() ? "Phone verification successful."
                : "Phone verification failed.";
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .result(result)
                .message(message)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the roles of the authenticated user.
     *
     * @param token    the access token from the cookie
     * @param response the HTTP response to handle cookies
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a list of roles
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - invalid token (ErrorCode.INVALID_TOKEN)
     * @implNote Fetches user roles from {@link IAccountService} and returns the list in an {@link ApiResponse}.
     */
    @GetMapping("/role")
    @Operation(summary = "Get user roles",
            description = "Retrieves the roles of the authenticated user.")
    @Parameter(name = "token", description = "Access token from cookie")
    public ResponseEntity<ApiResponse<List<String>>> getMyInfo(@CookieValue(name = "accessToken", required = false) String token, HttpServletResponse response) {
        ApiResponse<List<String>> apiResponse = ApiResponse.<List<String>>builder()
                .result(accountService.getRolesUser(token, response))
                .message("Get info successfully!")
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}