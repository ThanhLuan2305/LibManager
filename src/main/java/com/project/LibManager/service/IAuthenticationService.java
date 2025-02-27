package com.project.LibManager.service;

import java.text.ParseException;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.project.LibManager.constant.TokenType;
import com.project.LibManager.dto.request.AuthenticationRequest;
import com.project.LibManager.dto.request.ChangeMailRequest;
import com.project.LibManager.dto.request.ChangePasswordRequest;
import com.project.LibManager.dto.request.LogoutRequest;
import com.project.LibManager.dto.request.RegisterRequest;
import com.project.LibManager.dto.request.TokenRequest;
import com.project.LibManager.dto.request.UserCreateRequest;
import com.project.LibManager.dto.request.VerifyChangeMailRequest;
import com.project.LibManager.dto.response.AuthenticationResponse;
import com.project.LibManager.dto.response.ChangePassAfterResetRequest;
import com.project.LibManager.dto.response.IntrospectResponse;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.entity.User;


public interface IAuthenticationService {

    public AuthenticationResponse authenticate(AuthenticationRequest aRequest) ;

    String generateToken(User user, TokenType tokenType);
    
    // Verify token
    IntrospectResponse introspectToken(TokenRequest iRequest);

    // Logout user
    void logout(LogoutRequest logoutRequest) throws ParseException, JOSEException;

    SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException;

    AuthenticationResponse refreshToken(TokenRequest refreshRequest) throws JOSEException, ParseException;

    UserResponse registerUser(RegisterRequest registerRequest);

    boolean verifyEmail(String token) throws JOSEException, ParseException;

    String buildScope(User user);

    boolean changePassword(ChangePasswordRequest cpRequest);

    void forgetPassword(String email);

    Integer generateOTP(String email);

    String verifyOTP(Integer token, String email);

    String generatePassword(int length);

    String resetPassword(String token) throws JOSEException, ParseException ;

    boolean changePasswordAfterReset(ChangePassAfterResetRequest cpRequest);

    void verifyChangeEmail(VerifyChangeMailRequest changeMailRequest);
    
    void changeEmail(ChangeMailRequest cMailRequest) ;
}
