package com.project.libmanager.controller.common;

import com.project.libmanager.exception.AppException;
import com.project.libmanager.service.IAuthenticationService;
import com.project.libmanager.service.IUserService;
import com.project.libmanager.service.dto.request.AuthenticationRequest;
import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.AuthenticationResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CookieValue;

/**
 * REST controller for managing user authentication operations.
 * Provides endpoints for login, logout, token refresh, and retrieving user information.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
@Tag(name = "Authentication Management", description = "Endpoints for user authentication and session management")
public class AuthenticationController {
    private final IAuthenticationService aService;
    private final IUserService userService;

    /**
     * Authenticates a user and returns access and refresh tokens.
     *
     * @param aRequest the authentication request containing email and password
     * @param response the HTTP response to set cookies
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with an {@link AuthenticationResponse} containing the tokens
     * @throws AppException if:
     *                      - invalid credentials (ErrorCode.INVALID_CREDENTIALS)
     *                      - user not verified (ErrorCode.USER_NOT_VERIFIED)
     * @implNote Delegates authentication to {@link IAuthenticationService} and returns the tokens in an {@link ApiResponse}.
     */
    @PostMapping("/login")
    @Operation(summary = "User login",
            description = "Authenticates a user and returns access and refresh tokens.")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(
            @Valid @RequestBody AuthenticationRequest aRequest, HttpServletResponse response) {
        AuthenticationResponse rs = aService.authenticate(aRequest, response);
        ApiResponse<AuthenticationResponse> apiResponse = ApiResponse.<AuthenticationResponse>builder()
                .result(rs)
                .message("Login successfully!")
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Logs out the authenticated user and invalidates the session.
     *
     * @param accessToken the access token from the cookie
     * @param response    the HTTP response to clear cookies
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a success message
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - invalid token (ErrorCode.INVALID_TOKEN)
     * @implNote Delegates logout to {@link IAuthenticationService} and returns a success message in an {@link ApiResponse}.
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout",
            description = "Logs out the authenticated user and invalidates the session.")
    @Parameter(name = "accessToken", description = "Access token from cookie")
    public ResponseEntity<ApiResponse<String>> logout(
            @CookieValue(name = "accessToken", required = false) String accessToken, HttpServletResponse response) {
        aService.logout(accessToken, response);
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .message("Logout successfully")
                .result("success")
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Refreshes the access token using the refresh token.
     *
     * @param refreshToken the refresh token from the cookie
     * @param response     the HTTP response to set new cookies
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with an {@link AuthenticationResponse} containing the new tokens
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - invalid or expired refresh token (ErrorCode.INVALID_TOKEN)
     * @implNote Delegates token refresh to {@link IAuthenticationService} and returns the new tokens in an {@link ApiResponse}.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token",
            description = "Refreshes the access token using the refresh token.")
    @Parameter(name = "refreshToken", description = "Refresh token from cookie")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        AuthenticationResponse result = aService.refreshToken(refreshToken, response);
        ApiResponse<AuthenticationResponse> apiResponse = ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .message("Token refreshed successfully.")
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Retrieves the authenticated user's information.
     *
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a {@link UserResponse} detailing the user's information
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     * @implNote Fetches user info from {@link IUserService} and returns the details in an {@link ApiResponse}.
     */
    @GetMapping("/info")
    @Operation(summary = "Get user info",
            description = "Retrieves the authenticated user's information.")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo() {
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .message("Get info successfully!")
                .build();
        return ResponseEntity.ok(response);
    }
}