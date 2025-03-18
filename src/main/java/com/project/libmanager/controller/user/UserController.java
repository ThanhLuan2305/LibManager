package com.project.libmanager.controller.user;

import com.project.libmanager.service.IAccountService;
import com.project.libmanager.service.IPasswordService;
import com.project.libmanager.service.dto.request.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.UserResponse;
import com.project.libmanager.service.IUserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("user/users")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "JWT Authentication")
public class UserController {
    private final IUserService userService;
    private final IAccountService acountService;
    private final IPasswordService passwordService;
    private static final String RS_SUCCESS = "success";

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo() {
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .message("Get info successfully!")
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Boolean>> changePassword(@RequestBody @Valid ChangePasswordRequest cpRequest) {
        Boolean result = passwordService.changePassword(cpRequest);
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .message("Change password successfully")
                .result(result)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-mail")
    public ResponseEntity<ApiResponse<String>> changeMail(@RequestBody @Valid ChangeMailRequest emailRequest) {
        acountService.changeEmail(emailRequest);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Please verify your new email to change email")
                .result(RS_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/verify-change-mail")
    public ResponseEntity<ApiResponse<String>> verifyChangeMail(@RequestBody @Valid VerifyChangeMailRequest emailRequest) {
        acountService.verifyChangeEmail(emailRequest);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Change email successfully, you can login with new email")
                .result(RS_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-phone")
    public ResponseEntity<ApiResponse<String>> changePhone(@RequestBody @Valid ChangePhoneRequest phoneRequest) {
        acountService.changePhone(phoneRequest);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Please verify your new phone to change phone")
                .result(RS_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/verify-change-phone")
    public ResponseEntity<ApiResponse<String>> verifyChangePhone(@RequestBody @Valid VerifyChangePhoneRequest phoneRequest) {
        acountService.verifyChangePhone(phoneRequest);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Change phone successfully, you can login with new phone")
                .result(RS_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }
}
