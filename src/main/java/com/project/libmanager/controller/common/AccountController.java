package com.project.libmanager.controller.common;

import com.project.libmanager.service.IAccountService;
import com.project.libmanager.service.dto.request.RegisterRequest;
import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
public class AccountController {
    private final IAccountService accountService;
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody @Valid RegisterRequest registerRequest) {
        UserResponse result = accountService.registerUser(registerRequest);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .result(result)
                .message("Register successfully, please verify your email and phone to login!")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify-email")
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

    @PostMapping("/verify-phone")
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

    @GetMapping("/role")
    public ResponseEntity<ApiResponse<List<String>>> getMyInfo(@CookieValue(name = "accessToken", required = false) String token, HttpServletResponse response) {
        ApiResponse<List<String>> apiResponse = ApiResponse.<List<String>>builder()
                .result(accountService.getRolesUser(token, response))
                .message("Get info successfully!")
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
