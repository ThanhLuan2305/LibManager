package com.project.LibManager.controller;

import java.text.ParseException;

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
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest aRequest) {
        var rs = aService.authenticate(aRequest);
        return ApiResponse.<AuthenticationResponse>builder()
                          .result(rs)
                          .build();
    }
    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspectToken(@RequestBody TokenRequest introspectRequest) throws JOSEException, ParseException {
        return ApiResponse.<IntrospectResponse>builder()
                          .result(aService.introspectToken(introspectRequest))
                          .build();
    }
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody TokenRequest lRequest) throws ParseException, Exception {
        aService.logout(lRequest);
        return ApiResponse.<Void>builder()
                          .message("Logout successfully")
                          .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refreshToken(@RequestBody TokenRequest rfRequest) throws JOSEException, ParseException {
        return ApiResponse.<AuthenticationResponse>builder()
                          .result(aService.refreshToken(rfRequest))
                          .build();
    }

    @GetMapping("/verify-email")
    public ApiResponse<Boolean> verifyEmail(@RequestParam("token") String token) throws JOSEException, ParseException {
        return ApiResponse.<Boolean>builder()
                          .result(aService.verifyEmail(token))
                          .message("Verify email successfully")
                          .build();
    }

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody @Valid UserCreateRequest ucrRequest) throws JOSEException, ParseException {
        return ApiResponse.<UserResponse>builder()
                          .result(aService.registerUser(ucrRequest))
                          .message("Register successfully, please verify your email to login!")
                          .build();
    }

    @PostMapping("/change-password")
    public ApiResponse<Boolean> changePassword(@RequestBody ChangePasswordRequest cpRequest) throws JOSEException, ParseException {
        boolean rs = aService.changePassword(cpRequest);
        return ApiResponse.<Boolean>builder()
                          .message("Change password successfully")
                          .result(rs)
                          .build();
    }

    @PostMapping("/forget-password")
    public ApiResponse<String> forgetPassword(@RequestParam("email") String email) throws JOSEException, ParseException {
        aService.forgetPassword(email);
        return ApiResponse.<String>builder()
                          .message("Please check your email to reset password")
                          .result("success")
                          .build();
    }

    @PostMapping("/verify-otp")
    public ApiResponse<AuthenticationResponse> verifyOtp(@RequestParam("otp") Integer otp, @RequestParam("email") String email ) throws JOSEException, ParseException {
        return ApiResponse.<AuthenticationResponse>builder()
                          .message("Verify OTP successfully")
                          .result(aService.verifyOTP(otp, email))
                          .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPasssword(@RequestBody TokenRequest tokenRequest ) throws Exception {
        return ApiResponse.<String>builder()
                          .message("Reset password successfully, you can login with new password")
                          .result(aService.resetPassword(tokenRequest.getToken()))
                          .build();
    }

   @PostMapping("/change-mail")
   public ApiResponse<String> changeMail(@RequestBody ChangeMailRequest eMailRequest ) throws Exception {
        aService.changeEmail(eMailRequest);
        return ApiResponse.<String>builder()
                          .message("Please verify your new email to change new email")
                          .result("success")
                          .build();
    }
    @PostMapping("/verify-change-mail")
    public ApiResponse<String> verifyChangeMail(@RequestBody VerifyChangeMailRequest eMailRequest ) throws Exception {
        aService.verifyChangeEmail(eMailRequest);
        return ApiResponse.<String>builder()
                          .message("Change email successfully, you can login with new email")
                          .result("success")
                          .build();
    }
}
