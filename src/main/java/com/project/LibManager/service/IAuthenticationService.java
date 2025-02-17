package com.project.LibManager.service;

import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Random;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.LibManager.constant.ErrorCode;
import com.project.LibManager.constant.PredefinedRole;
import com.project.LibManager.dto.request.AuthenticationRequest;
import com.project.LibManager.dto.request.ChangeMailRequest;
import com.project.LibManager.dto.request.ChangePasswordRequest;
import com.project.LibManager.dto.request.TokenRequest;
import com.project.LibManager.dto.request.UserCreateRequest;
import com.project.LibManager.dto.request.VerifyChangeMailRequest;
import com.project.LibManager.dto.response.AuthenticationResponse;
import com.project.LibManager.dto.response.IntrospectResponse;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.entity.User;


public interface IAuthenticationService {

    public AuthenticationResponse authenticate(AuthenticationRequest aRequest) ;

    String generateToken(User user, boolean verifyEmail);
    
    // Verify token
    IntrospectResponse introspectToken(TokenRequest iRequest);

    // Logout user
    void logout(TokenRequest aRequest) throws ParseException, JOSEException;

    SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException;

    AuthenticationResponse refreshToken(TokenRequest refreshRequest) throws JOSEException, ParseException;

    UserResponse registerUser(UserCreateRequest userCreateRequest);

    boolean verifyEmail(String token) throws JOSEException, ParseException;

    String buildScope(User user);

    boolean changePassword(ChangePasswordRequest cpRequest);
    void forgetPassword(String email);
    Integer generateOTP(String email);
    AuthenticationResponse verifyOTP(Integer token, String email);

    String generatePassword(int length);

    public String resetPassword(String token) throws JOSEException, ParseException ;

    void verifyChangeEmail(VerifyChangeMailRequest changeMailRequest);
    
    void changeEmail(ChangeMailRequest cMailRequest) ;
}
