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
import com.project.LibManager.dto.request.TokenRequest;
import com.project.LibManager.dto.request.UserCreateRequest;
import com.project.LibManager.dto.response.ApiResponse;
import com.project.LibManager.dto.response.AuthenticationResponse;
import com.project.LibManager.dto.response.IntrospectResponse;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.service.AuthenticationService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthenticationController {
    AuthenticationService aService;

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest aRequest) {
        var rs = aService.authenticate(aRequest);
        return ApiResponse.<AuthenticationResponse>builder()
                          .result(rs)
                          .build();
    }
    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspectToken(@RequestBody TokenRequest introspectRequest) throws JOSEException, ParseException {
        return ApiResponse.<IntrospectResponse>builder()
                          .result(aService.introspectToken(introspectRequest))
                          .build();
    }
    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody TokenRequest lRequest) throws ParseException, Exception {
        aService.logout(lRequest);
        return ApiResponse.<Void>builder()
                          .message("Logout successfully")
                          .build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refreshToken(@RequestBody TokenRequest rfRequest) throws JOSEException, ParseException {
        return ApiResponse.<AuthenticationResponse>builder()
                          .result(aService.refreshToken(rfRequest))
                          .build();
    }

    @GetMapping("/verify-email")
    ApiResponse<Boolean> verifyEmail(@RequestParam("token") String token) throws JOSEException, ParseException {
        return ApiResponse.<Boolean>builder()
                          .result(aService.verifyEmail(token))
                          .message("Verify email successfully")
                          .build();
    }

    @PostMapping("/register")
    ApiResponse<String> register(@RequestBody UserCreateRequest ucrRequest) throws JOSEException, ParseException {
        return ApiResponse.<String>builder()
                          .result(aService.registerUser(ucrRequest))
                          .message("Register successfully, please verify your email to login!")
                          .build();
    }
}
