package com.project.libmanager.sercurity;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.constant.TokenType;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.service.dto.request.TokenRequest;
import com.project.libmanager.service.dto.response.IntrospectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

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

    @Value("${jwt.mail-duration}")
    private Long mailDuration;

    public String generateToken(User user, TokenType tokenType) {
        try {
            // Header
            JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

            long duration;
            switch (tokenType) {
                case REFRESH -> duration = refreshDuration;
                case VERIFY_MAIL -> duration = mailDuration;
                default -> duration = validDuration;
            }

            // JWT claims
            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getEmail())
                    .issuer("NTL")
                    .issueTime(new Date())
                    .expirationTime(new Date(Instant.now().plus(duration, ChronoUnit.SECONDS).toEpochMilli()))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("scope", buildScope(user))
                    .claim("type", tokenType.name())
                    .build();

            // Build Token
            JWSObject jwsObject = new JWSObject(jwsHeader, new Payload(jwtClaimsSet.toJSONObject()));

            // Signature
            jwsObject.sign(new MACSigner(signKey.getBytes()));

            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        if (!user.getRoles().isEmpty()) {
            user.getRoles().forEach(role -> stringJoiner.add("ROLE_" + role.getName()));
        }
        return stringJoiner.toString();
    }

    public IntrospectResponse introspectToken(TokenRequest iRequest) {
        String token = iRequest.getToken();
        boolean invalid = true;

        try {
            verifyToken(token, false);
            return IntrospectResponse.builder().valid(invalid).build();
        } catch (Exception e) {
            log.error(token, "is error: {}", e.getMessage());
            throw new AppException(ErrorCode.JWT_TOKEN_INVALID);
        }

    }

    public SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(signKey.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);
        // check verify or refresh token
        Date expTime = (isRefresh) ? new Date(signedJWT.getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(refreshDuration, ChronoUnit.SECONDS)
                .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean rs = signedJWT.verify(verifier);

        if (!expTime.after(new Date())) {
            log.error("Token expired");
            throw new AppException(ErrorCode.JWT_TOKEN_EXPIRED);
        }

        if (!rs) {
            log.error("Token invalid");
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        }

        return signedJWT;
    }
}
