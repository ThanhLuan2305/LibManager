package com.project.libmanager.security;


import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.constant.TokenType;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    @Value("${jwt.signing.key}")
    private String signKey;

    @Value("${jwt.valid-duration}")
    private Long validDuration;

    @Value("${jwt.refresh-duration}")
    private Long refreshDuration;

    private static final String ISSUER = "NTL";
    private static final JWSAlgorithm SIGNING_ALGORITHM = JWSAlgorithm.HS512;

    public String generateToken(User user, TokenType tokenType, String jti) {
        long duration = (tokenType == TokenType.REFRESH) ? refreshDuration : validDuration;
        return buildToken(user, tokenType, duration, jti);
    }

    public String renewRefreshToken(User user, String jti) {
        return buildToken(user, TokenType.REFRESH, refreshDuration, jti);
    }

    private String buildToken(User user, TokenType tokenType, long duration, String jti) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getEmail())
                    .issuer(ISSUER)
                    .issueTime(new Date())
                    .expirationTime(Date.from(Instant.now().plus(duration, ChronoUnit.SECONDS)))
                    .jwtID(jti)
                    .claim("scope", buildScope(user))
                    .claim("type", tokenType.name())
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(SIGNING_ALGORITHM), claims);
            signedJWT.sign(new MACSigner(signKey.getBytes()));

            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error("Error creating token: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public SignedJWT verifyToken(String token, boolean isRefresh) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(new MACVerifier(signKey.getBytes()))) {
                log.error("Invalid token");
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            Date expiration = isRefresh
                    ? Date.from(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(refreshDuration, ChronoUnit.SECONDS))
                    : signedJWT.getJWTClaimsSet().getExpirationTime();

            if (expiration.before(new Date())) {
                log.error("Token expired");
                throw new AppException(ErrorCode.JWT_TOKEN_EXPIRED);
            }

            return signedJWT;
        } catch (Exception e) {
            log.error("Error verifying token", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private String buildScope(User user) {
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        return Optional.ofNullable(user.getRoles())
                .map(roles -> roles.stream().map(role -> "ROLE_" + role.getName()).toList())
                .map(roleList -> String.join(" ", roleList))
                .orElse("");
    }

}
