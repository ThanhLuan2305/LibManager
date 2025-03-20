package com.project.libmanager.controller.common;

import com.project.libmanager.service.IPasswordService;
import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.ChangePassAfterResetRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
public class PasswordController {
    private final IPasswordService passwordService;

    @PutMapping("/forget-password")
    public ResponseEntity<ApiResponse<String>> forgetPassword(@RequestParam("contactInfo") String contactInfo,
                                                              @RequestParam("isPhone") boolean isPhone) {
        passwordService.forgetPassword(contactInfo, isPhone);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Please check your email to reset password")
                .result("success")
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestParam("otp") String otp,
                                                             @RequestParam("contactInfo") String contactInfo,
                                                             @RequestParam("isPhone") boolean isPhone) {
        String result = passwordService.resetPassword(otp, contactInfo, isPhone);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Reset password successfully, you can change it and login with new password")
                .result(result)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password-after-reset")
    public ResponseEntity<ApiResponse<Boolean>> changePasswordAfterReset(
            @RequestBody ChangePassAfterResetRequest cpRequest) {
        Boolean result = passwordService.changePasswordAfterReset(cpRequest);
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .message("Change password successfully")
                .result(result)
                .build();
        return ResponseEntity.ok(response);
    }
}
