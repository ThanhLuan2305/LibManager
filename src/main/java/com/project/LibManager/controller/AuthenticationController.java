package com.project.LibManager.controller;

import java.text.ParseException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.JOSEException;
import com.project.LibManager.dto.request.AuthenticationRequest;
import com.project.LibManager.dto.request.ChangeMailRequest;
import com.project.LibManager.dto.request.ChangePasswordRequest;
import com.project.LibManager.dto.request.LogoutRequest;
import com.project.LibManager.dto.request.TokenRequest;
import com.project.LibManager.dto.request.UserCreateRequest;
import com.project.LibManager.dto.request.VerifyChangeMailRequest;
import com.project.LibManager.dto.response.ApiResponse;
import com.project.LibManager.dto.response.AuthenticationResponse;
import com.project.LibManager.dto.response.IntrospectResponse;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.service.IAuthenticationService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
public class AuthenticationController {
    private final IAuthenticationService aService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(@RequestBody AuthenticationRequest aRequest) {
        var rs = aService.authenticate(aRequest);
        ApiResponse<AuthenticationResponse> response = ApiResponse.<AuthenticationResponse>builder()
                                                                  .result(rs)
                                                                  .build();
        return ResponseEntity.ok(response);

    }

    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<IntrospectResponse>> introspectToken(@RequestBody TokenRequest introspectRequest) throws JOSEException, ParseException {
        IntrospectResponse result = aService.introspectToken(introspectRequest);
        ApiResponse<IntrospectResponse> response = ApiResponse.<IntrospectResponse>builder()
                                                              .result(result)
                                                              .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody LogoutRequest lRequest) throws ParseException, Exception {
        aService.logout(lRequest);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                                               .message("Logout successfully")
                                               .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(@RequestBody TokenRequest rfRequest) throws JOSEException, ParseException {
        AuthenticationResponse result = aService.refreshToken(rfRequest);
        ApiResponse<AuthenticationResponse> response = ApiResponse.<AuthenticationResponse>builder()
                                                                  .result(result)
                                                                  .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Boolean>> verifyEmail(@RequestParam("token") String token) throws JOSEException, ParseException {
        Boolean result = aService.verifyEmail(token);
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                                                  .result(result)
                                                  .message("Verify email successfully")
                                                  .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody @Valid UserCreateRequest ucrRequest) throws JOSEException, ParseException {
        UserResponse result = aService.registerUser(ucrRequest);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                                                        .result(result)
                                                        .message("Register successfully, please verify your email to login!")
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

    @PostMapping("/forget-password")
    public ResponseEntity<ApiResponse<String>> forgetPassword(@RequestParam("email") String email) throws JOSEException, ParseException {
        aService.forgetPassword(email);
        ApiResponse<String> response = ApiResponse.<String>builder()
                                                 .message("Please check your email to reset password")
                                                 .result("success")
                                                 .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@RequestParam("otp") Integer otp, @RequestParam("email") String email) throws JOSEException, ParseException {
        String result = aService.verifyOTP(otp, email);
        ApiResponse<String> response = ApiResponse.<String>builder()
                                                                  .message("Verify OTP successfully")
                                                                  .result(result)
                                                                  .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody TokenRequest tokenRequest) throws Exception {
        String result = aService.resetPassword(tokenRequest.getToken());
        ApiResponse<String> response = ApiResponse.<String>builder()
                                                 .message("Reset password successfully, you can login with new password")
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
