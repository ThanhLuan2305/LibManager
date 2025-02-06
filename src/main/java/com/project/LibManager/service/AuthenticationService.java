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
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.LibManager.dto.request.AuthenticationRequest;
import com.project.LibManager.dto.request.TokenRequest;
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

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected Long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refresh-duration}")
    protected Long REFRESH_DURATION;

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
                                    Instant.now().plus(VALID_DURATION,ChronoUnit.SECONDS).toEpochMilli()
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
    public IntrospectResponse introspectToken(TokenRequest iRequest) throws JOSEException, ParseException {
        String token = iRequest.getToken();
        boolean invalid = true;

        try {
            verifyToken(token, false);
        } catch (Exception e) {
            invalid = false;
        }

        return IntrospectResponse.builder().valid(invalid).build(); 
    }

    // Logout user
    public void logout(TokenRequest aRequest) throws Exception, ParseException {
        try {
            var signToken = verifyToken(aRequest.getToken(), false); 

            String jwtID = signToken.getJWTClaimsSet().getJWTID();
            Date expTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidateToken invalidateToken = InvalidateToken.builder()
                    .id(jwtID)
                    .expiryTime(expTime)
                    .build();

            invalidateTokenRepository.save(invalidateToken); 
        } catch (AppException e) {
            log.info("Token already expired");
        }   
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGN_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);
        // check verify or refresh token
        Date expTime = (isRefresh) ? new Date(signedJWT.getJWTClaimsSet()
                                                .getIssueTime()
                                                .toInstant()
                                                .plus(REFRESH_DURATION, ChronoUnit.SECONDS)
                                                .toEpochMilli())
                                   : signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean rs = signedJWT.verify(verifier);

        if(!(rs && expTime.after(new Date()))) 
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if(invalidateTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        
        return signedJWT;
    }

    public AuthenticationResponse refreshToken(TokenRequest refreshRequest) throws JOSEException, ParseException {
        var signedJWT = verifyToken(refreshRequest.getToken(), true);

        String jwtID = signedJWT.getJWTClaimsSet().getJWTID();
        Date expTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        InvalidateToken invalidateToken = InvalidateToken.builder()
                .id(jwtID)
                .expiryTime(expTime)
                .build();
        invalidateTokenRepository.save(invalidateToken);   

        String email = signedJWT.getJWTClaimsSet().getSubject();

        User user = userRepository.findByEmail(email);
        if(user == null) 
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        String token = generateToken(email);
        return AuthenticationResponse.builder().authenticate(true).token(token).build();

    }
}
