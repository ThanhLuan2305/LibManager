package com.project.LibManager.sercurity;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.project.LibManager.constant.ErrorCode;
import com.project.LibManager.constant.TokenType;
import com.project.LibManager.entity.User;
import com.project.LibManager.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
}
