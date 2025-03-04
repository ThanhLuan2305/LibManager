package com.project.LibManager.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.project.LibManager.constant.TokenType;
import com.project.LibManager.dto.request.*;
import com.project.LibManager.dto.response.AuthenticationResponse;
import com.project.LibManager.dto.response.ChangePassAfterResetRequest;
import com.project.LibManager.dto.response.IntrospectResponse;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.entity.User;

import java.text.ParseException;

public interface IAuthenticationService {

    AuthenticationResponse authenticate(AuthenticationRequest aRequest);

    // Verify token
    IntrospectResponse introspectToken(TokenRequest iRequest);

    // Logout user
    void logout(LogoutRequest logoutRequest) throws ParseException, JOSEException;

    SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException;

    AuthenticationResponse refreshToken(TokenRequest refreshRequest) throws JOSEException, ParseException;

    UserResponse registerUser(RegisterRequest registerRequest);

    boolean verifyEmail(String token) throws JOSEException, ParseException;

    boolean changePassword(ChangePasswordRequest cpRequest);

    void forgetPassword(String email);

    String verifyOTP(Integer token, String email);

    String resetPassword(String token) throws JOSEException, ParseException;

    boolean changePasswordAfterReset(ChangePassAfterResetRequest cpRequest);

    void verifyChangeEmail(VerifyChangeMailRequest changeMailRequest);

    void changeEmail(ChangeMailRequest cMailRequest);
}
