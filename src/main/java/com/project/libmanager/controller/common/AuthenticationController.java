package com.project.libmanager.controller.common;

import com.project.libmanager.service.IAuthenticationService;
import com.project.libmanager.service.dto.request.AuthenticationRequest;
import com.project.libmanager.service.dto.request.TokenRequest;
import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.AuthenticationResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
public class AuthenticationController {
    private final IAuthenticationService aService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(
            @Valid @RequestBody AuthenticationRequest aRequest, HttpServletResponse response) {
        AuthenticationResponse rs = aService.authenticate(aRequest, response);
        ApiResponse<AuthenticationResponse> apiResponse = ApiResponse.<AuthenticationResponse>builder()
                .result(rs)
                .message("Login successfully!")
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestBody TokenRequest logoutRequest, HttpServletResponse response) {
        aService.logout(logoutRequest, response);
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .message("Logout successfully")
                .result("success")
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
            @RequestBody TokenRequest rfRequest, HttpServletResponse response) {
        AuthenticationResponse result = aService.refreshToken(rfRequest, response);
        ApiResponse<AuthenticationResponse> apiResponse = ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .message("Token refreshed successfully.")
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
