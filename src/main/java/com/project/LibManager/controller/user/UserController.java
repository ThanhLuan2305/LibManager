package com.project.LibManager.controller.user;

import java.text.ParseException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.JOSEException;
import com.project.LibManager.dto.request.ChangeMailRequest;
import com.project.LibManager.dto.request.ChangePasswordRequest;
import com.project.LibManager.dto.request.VerifyChangeMailRequest;
import com.project.LibManager.dto.response.ApiResponse;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.service.IAuthenticationService;
import com.project.LibManager.service.IUserService;
import com.project.LibManager.specification.UserQueryService;

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
    private final IAuthenticationService aService;

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo() {
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .message("Get info successfully!")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Boolean>> changePassword(@RequestBody ChangePasswordRequest cpRequest) throws JOSEException, ParseException {
        Boolean result = aService.changePassword(cpRequest);
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                                                  .message("Change password successfully")
                                                  .result(result)
                                                  .build();
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/change-mail")
    public ResponseEntity<ApiResponse<String>> changeMail(@RequestBody ChangeMailRequest eMailRequest) throws Exception {
        aService.changeEmail(eMailRequest);
        ApiResponse<String> response = ApiResponse.<String>builder()
                                                 .message("Please verify your new email to change new email")
                                                 .result("success")
                                                 .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-change-mail")
    public ResponseEntity<ApiResponse<String>> verifyChangeMail(@RequestBody VerifyChangeMailRequest eMailRequest) throws Exception {
        aService.verifyChangeEmail(eMailRequest);
        ApiResponse<String> response = ApiResponse.<String>builder()
                                                 .message("Change email successfully, you can login with new email")
                                                 .result("success")
                                                 .build();
        return ResponseEntity.ok(response);
    }
}
