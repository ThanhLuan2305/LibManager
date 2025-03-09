package com.project.libmanager.controller.user;

import com.project.libmanager.service.IAccountService;
import com.project.libmanager.service.IPasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.libmanager.service.dto.request.ChangeMailRequest;
import com.project.libmanager.service.dto.request.ChangePasswordRequest;
import com.project.libmanager.service.dto.request.VerifyChangeMailRequest;
import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.UserResponse;
import com.project.libmanager.service.IAuthenticationService;
import com.project.libmanager.service.IUserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("user/users")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "JWT Authentication")
public class UserController {
    private final IUserService userService;
    private final IAccountService acountService;
    private final IPasswordService passwordService;

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo() {
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .message("Get info successfully!")
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Boolean>> changePassword(@RequestBody ChangePasswordRequest cpRequest) {
        Boolean result = passwordService.changePassword(cpRequest);
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .message("Change password successfully")
                .result(result)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-mail")
    public ResponseEntity<ApiResponse<String>> changeMail(@RequestBody ChangeMailRequest eMailRequest) {
        acountService.changeEmail(eMailRequest);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Please verify your new email to change new email")
                .result("success")
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/verify-change-mail")
    public ResponseEntity<ApiResponse<String>> verifyChangeMail(@RequestBody VerifyChangeMailRequest eMailRequest) {
        acountService.verifyChangeEmail(eMailRequest);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Change email successfully, you can login with new email")
                .result("success")
                .build();
        return ResponseEntity.ok(response);
    }
}
