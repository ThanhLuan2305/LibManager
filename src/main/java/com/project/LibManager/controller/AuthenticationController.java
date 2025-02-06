package com.project.LibManager.controller;

import java.text.ParseException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.JOSEException;
import com.project.LibManager.dto.request.AuthenticationRequest;
import com.project.LibManager.dto.request.IntrospectRequest;
import com.project.LibManager.dto.request.LogoutRequest;
import com.project.LibManager.dto.response.ApiResponse;
import com.project.LibManager.dto.response.AuthenticationResponse;
import com.project.LibManager.dto.response.IntrospectResponse;
import com.project.LibManager.service.AuthenticationService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.var;
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
    ApiResponse<IntrospectResponse> introspectToken(@RequestBody IntrospectRequest introspectRequest) throws JOSEException, ParseException {
        return ApiResponse.<IntrospectResponse>builder()
                          .result(aService.introspectToken(introspectRequest))
                          .build();
    }
    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest lRequest) throws ParseException, Exception {
        aService.logout(lRequest);
        return ApiResponse.<Void>builder()
                          .message("Logout successfully")
                          .build();
    }
}
