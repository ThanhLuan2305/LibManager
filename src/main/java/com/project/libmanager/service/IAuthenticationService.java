package com.project.libmanager.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.project.libmanager.service.dto.request.AuthenticationRequest;
import com.project.libmanager.service.dto.request.LogoutRequest;
import com.project.libmanager.service.dto.request.TokenRequest;
import com.project.libmanager.service.dto.response.AuthenticationResponse;
import com.project.libmanager.service.dto.response.IntrospectResponse;

import java.text.ParseException;

public interface IAuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);

    AuthenticationResponse refreshToken(TokenRequest refreshRequest) throws JOSEException, ParseException;

    void logout(LogoutRequest logoutRequest) throws ParseException, JOSEException;
}
