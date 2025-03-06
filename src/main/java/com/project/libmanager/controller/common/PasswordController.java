package com.project.libmanager.controller.common;

import com.nimbusds.jose.JOSEException;
import com.project.libmanager.service.IPasswordService;
import com.project.libmanager.service.dto.request.TokenRequest;
import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.ChangePassAfterResetRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

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

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@RequestParam("otp") Integer otp,
                                                         @RequestParam("email") String email) {
        String result = passwordService.verifyOTP(otp, email);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Verify OTP successfully")
                .result(result)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody TokenRequest tokenRequest)
            throws JOSEException, ParseException {
        String result = passwordService.resetPassword(tokenRequest.getToken());
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Reset password successfully, you can login with new password")
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
