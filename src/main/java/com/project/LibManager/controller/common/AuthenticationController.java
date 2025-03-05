package com.project.LibManager.controller.common;

import java.text.ParseException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nimbusds.jose.JOSEException;
import com.project.LibManager.service.dto.request.AuthenticationRequest;
import com.project.LibManager.service.dto.request.LogoutRequest;
import com.project.LibManager.service.dto.request.RegisterRequest;
import com.project.LibManager.service.dto.request.TokenRequest;
import com.project.LibManager.service.dto.response.ApiResponse;
import com.project.LibManager.service.dto.response.AuthenticationResponse;
import com.project.LibManager.service.dto.response.ChangePassAfterResetRequest;
import com.project.LibManager.service.dto.response.IntrospectResponse;
import com.project.LibManager.service.dto.response.UserResponse;
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
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(
            @RequestBody AuthenticationRequest aRequest) {
        var rs = aService.authenticate(aRequest);
        String mess = rs.isForceChangePassword() ? "You must change new password before login" : "Login successfully!";
        ApiResponse<AuthenticationResponse> response = ApiResponse.<AuthenticationResponse>builder()
                .result(rs)
                .message(mess)
                .build();
        return ResponseEntity.ok(response);

    }

    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<IntrospectResponse>> introspectToken(
            @RequestBody TokenRequest introspectRequest) {
        IntrospectResponse result = aService.introspectToken(introspectRequest);
        ApiResponse<IntrospectResponse> response = ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .message("Token introspection successful.")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestBody LogoutRequest lRequest)
            throws ParseException, JOSEException {
        aService.logout(lRequest);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Logout successfully")
                .result("success")
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(@RequestBody TokenRequest rfRequest)
            throws JOSEException, ParseException {
        AuthenticationResponse result = aService.refreshToken(rfRequest);
        ApiResponse<AuthenticationResponse> response = ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .message("Token refreshed successfully.")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Boolean>> verifyEmail(@RequestParam("token") String token)
            throws JOSEException, ParseException {
        Boolean result = aService.verifyEmail(token);
        String message = result.booleanValue() ? "Email verification successful."
                : "Email verification failed.";
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .result(result)
                .message(message)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody @Valid RegisterRequest registerRequest) {
        UserResponse result = aService.registerUser(registerRequest);
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .result(result)
                .message("Register successfully, please verify your email to login!")
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/forget-password")
    public ResponseEntity<ApiResponse<String>> forgetPassword(@RequestParam("email") String email) {
        aService.forgetPassword(email);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Please check your email to reset password")
                .result("success")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@RequestParam("otp") Integer otp,
            @RequestParam("email") String email) {
        String result = aService.verifyOTP(otp, email);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Verify OTP successfully")
                .result(result)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody TokenRequest tokenRequest)
            throws JOSEException, ParseException {
        String result = aService.resetPassword(tokenRequest.getToken());
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Reset password successfully, you can login with new password")
                .result(result)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password-after-reset")
    public ResponseEntity<ApiResponse<Boolean>> changePasswordAfterReset(
            @RequestBody ChangePassAfterResetRequest cpRequest) {
        Boolean result = aService.changePasswordAfterReset(cpRequest);
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .message("Change password successfully")
                .result(result)
                .build();
        return ResponseEntity.ok(response);
    }

}
