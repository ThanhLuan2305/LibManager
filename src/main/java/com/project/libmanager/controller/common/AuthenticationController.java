package com.project.libmanager.controller.common;

import com.project.libmanager.service.IAuthenticationService;
import com.project.libmanager.service.dto.request.AuthenticationRequest;
import com.project.libmanager.service.dto.request.TokenRequest;
import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.AuthenticationResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
public class AuthenticationController {
    private final IAuthenticationService aService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(
            @RequestBody AuthenticationRequest aRequest) {
        AuthenticationResponse rs = aService.authenticate(aRequest);
        ApiResponse<AuthenticationResponse> response = ApiResponse.<AuthenticationResponse>builder()
                .result(rs)
                .message("Login successfully!")
                .build();
        return ResponseEntity.ok(response);

    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestBody TokenRequest logoutRequest) {
        aService.logout(logoutRequest);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Logout successfully")
                .result("success")
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(@RequestBody TokenRequest rfRequest) {
        AuthenticationResponse result = aService.refreshToken(rfRequest);
        ApiResponse<AuthenticationResponse> response = ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .message("Token refreshed successfully.")
                .build();
        return ResponseEntity.ok(response);
    }


}
