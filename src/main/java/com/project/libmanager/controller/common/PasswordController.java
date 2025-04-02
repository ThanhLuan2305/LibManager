package com.project.libmanager.controller.common;

import com.project.libmanager.service.IPasswordService;
import com.project.libmanager.service.dto.request.ResetPasswordRequest;
import com.project.libmanager.service.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
public class PasswordController {
    private final IPasswordService passwordService;

    @PutMapping("/forget-password")
    public ResponseEntity<ApiResponse<String>> forgetPassword(@RequestParam("email") String email) {
        passwordService.forgetPassword(email);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Please check your email to reset password")
                .result("success")
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        passwordService.resetPassword(resetPasswordRequest);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Reset password successfully, you can login with new password")
                .result("success")
                .build();
        return ResponseEntity.ok(response);
    }
}
