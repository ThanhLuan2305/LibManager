package com.project.libmanager.service;

import com.project.libmanager.service.dto.request.AuthenticationRequest;
import com.project.libmanager.service.dto.request.TokenRequest;
import com.project.libmanager.service.dto.response.AuthenticationResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface IAuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletResponse response);

    AuthenticationResponse refreshToken(TokenRequest refreshRequest, HttpServletResponse response);

    void logout(TokenRequest logoutRequest, HttpServletResponse response) ;
}
