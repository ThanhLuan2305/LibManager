package com.project.LibManager.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.LibManager.dto.request.AuthenticationRequest;
import com.project.LibManager.dto.request.IntrospectRequest;
import com.project.LibManager.dto.request.LogoutRequest;
import com.project.LibManager.dto.response.AuthenticationResponse;
import com.project.LibManager.dto.response.IntrospectResponse;
import com.project.LibManager.entity.InvalidateToken;
import com.project.LibManager.entity.User;
import com.project.LibManager.exception.AppException;
import com.project.LibManager.exception.ErrorCode;
import com.project.LibManager.repository.InvalidateTokenRepository;
import com.project.LibManager.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthenticationService {
    UserRepository userRepository;
    InvalidateTokenRepository invalidateTokenRepository;

    @NonFinal
    @Value("${jwt.signing.key}")
    protected String SIGN_KEY;

    public AuthenticationResponse authenticate(AuthenticationRequest aRequest) {
        User user = userRepository.findByEmail(aRequest.getEmail());
        if(user == null) 
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        if(!user.getIsVerified()) 
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean rs = passwordEncoder.matches(aRequest.getPassword(), user.getPassword());
        if(!rs)
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        String token = generateToken(aRequest.getEmail());
        return AuthenticationResponse.builder().authenticate(rs).token(token).build();
    } 

    private String generateToken(String email) {
        //Header
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                                .subject(email).issuer("NTL")
                                .issueTime(new Date())
                                .expirationTime(new Date(
                                    Instant.now().plus(1,ChronoUnit.HOURS).toEpochMilli()
                                ))
                                .jwtID(UUID.randomUUID().toString())
                                .build();
        //Payload
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        //Build Token
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        //Signature
        try {
            jwsObject.sign(new MACSigner(SIGN_KEY.getBytes()));
        } catch (JOSEException e) {
            log.error("Cannot create token ", e);
            e.printStackTrace();
        }
        return jwsObject.serialize();
    }
    
    // Verify token
    public IntrospectResponse introspectToken(IntrospectRequest iRequest) throws JOSEException, ParseException {
        String token = iRequest.getToken();
        boolean invalid = true;

        try {
            verifyToken(token);
        } catch (Exception e) {
            invalid = false;
        }

        return IntrospectResponse.builder().valid(invalid).build(); 
    }

    // Logout user
    public void logout(LogoutRequest aRequest) throws Exception, ParseException {
        var signToken = verifyToken(aRequest.getToken()); 
        String jwtID = signToken.getJWTClaimsSet().getJWTID();
        Date expTime = signToken.getJWTClaimsSet().getExpirationTime();
        InvalidateToken invalidateToken = InvalidateToken.builder()
                .id(jwtID)
                .expiryTime(expTime)
                .build();
        invalidateTokenRepository.save(invalidateToken);    
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGN_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean rs = signedJWT.verify(verifier);

        if(!(rs && expTime.after(new Date()))) 
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if(invalidateTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        
        return signedJWT;
    }
}
