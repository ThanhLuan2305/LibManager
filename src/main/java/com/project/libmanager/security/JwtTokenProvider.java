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

/**
 * Utility class for generating and verifying JWT (JSON Web Tokens) for authentication.
 * Supports access, refresh, and reset password tokens with configurable durations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    @Value("${jwt.signing.key}")
    private String signKey;                // Secret key for signing/verifying tokens

    @Value("${jwt.valid-duration}")
    private long validDuration;            // Duration (seconds) for access token validity

    @Value("${jwt.refresh-duration}")
    private long refreshDuration;          // Duration (seconds) for refresh token validity

    @Value("${jwt.reset-duration}")
    private long resetDuration;            // Duration (seconds) for reset password token validity

    private static final String ISSUER = "NTL";                   // Token issuer identifier
    private static final JWSAlgorithm SIGNING_ALGORITHM = JWSAlgorithm.HS512; // HMAC-SHA512 algorithm for signing

    /**
     * Generates a JWT token for a user with specified type and JTI.
     *
     * @param user      the {@link User} for whom the token is generated
     * @param tokenType the {@link TokenType} (ACCESS, REFRESH, or RESET_PASSWORD)
     * @param jti       the JWT ID (unique identifier for the token)
     * @return a serialized JWT token string
     * @throws AppException if token creation fails (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Determines duration based on token type and delegates to buildToken.
     */
    public String generateToken(User user, TokenType tokenType, String jti) {
        // Select duration based on token type; defaults to access token duration
        long duration;
        if (tokenType.equals(TokenType.REFRESH)) {
            duration = refreshDuration;
        } else if (tokenType.equals(TokenType.RESET_PASSWORD)) {
            duration = resetDuration;
        } else {
            duration = validDuration;
        }
        // Log token generation; aids debugging
        log.info("Generating token: {}, duration: {}", tokenType, duration);
        // Delegate to buildToken; encapsulates core token creation logic
        return buildToken(user, tokenType, duration, jti);
    }

    /**
     * Renews a refresh token for a user with an existing JTI.
     *
     * @param user the {@link User} for whom the refresh token is renewed
     * @param jti  the existing JWT ID to reuse
     * @return a serialized JWT refresh token string
     * @throws AppException if token creation fails (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Reuses JTI to maintain session continuity and uses refresh duration.
     */
    public String renewRefreshToken(User user, String jti) {
        // Build refresh token; reuses JTI with configured refresh duration
        return buildToken(user, TokenType.REFRESH, refreshDuration, jti);
    }

    /**
     * Builds and signs a JWT token with user claims and specified parameters.
     *
     * @param user      the {@link User} for whom the token is created
     * @param tokenType the {@link TokenType} of the token
     * @param duration  the validity duration in seconds
     * @param jti       the JWT ID for uniqueness
     * @return a serialized JWT token string
     * @throws AppException if signing fails (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Constructs claims (email, scope, type), signs with HMAC-SHA512, and serializes.
     */
    private String buildToken(User user, TokenType tokenType, long duration, String jti) {
        try {
            // Build claims set; includes standard and custom claims
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getEmail())                           // User identifier
                    .issuer(ISSUER)                                     // Token issuer
                    .issueTime(new Date())                              // Current timestamp
                    .expirationTime(Date.from(Instant.now().plus(duration, ChronoUnit.SECONDS))) // Expiration
                    .jwtID(jti)                                         // Unique token ID
                    .claim("scope", buildScope(user))             // User roles as scope
                    .claim("type", tokenType.name())              // Token type
                    .build();

            // Create signed JWT; uses HS512 algorithm
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(SIGNING_ALGORITHM), claims);
            // Sign with secret key; ensures integrity and authenticity
            signedJWT.sign(new MACSigner(signKey.getBytes()));

            // Serialize to string; compact token format
            return signedJWT.serialize();
        } catch (JOSEException e) {
            // Log signing error; provides context for debugging
            log.error("Error creating token: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Verifies a JWT token's signature and expiration.
     *
     * @param token the JWT token string to verify
     * @return the {@link SignedJWT} object if valid
     * @throws AppException if:
     *                      - token is invalid (ErrorCode.UNAUTHENTICATED)
     *                      - token is expired (ErrorCode.JWT_TOKEN_EXPIRED)
     *                      - parsing/verification fails (ErrorCode.UNAUTHENTICATED)
     * @implNote Checks signature with secret key and ensures token hasn't expired.
     */
    public SignedJWT verifyToken(String token) {
        try {
            // Parse token; converts string to JWT object
            SignedJWT signedJWT = SignedJWT.parse(token);
            // Verify signature; uses HMAC-SHA512 and secret key
            if (!signedJWT.verify(new MACVerifier(signKey.getBytes()))) {
                log.error("Invalid token");
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            // Check expiration; compares with current time
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiration.before(new Date())) {
                log.error("Token expired");
                throw new AppException(ErrorCode.JWT_TOKEN_EXPIRED);
            }

            // Return verified token; allows further claim extraction
            return signedJWT;
        } catch (Exception e) {
            // Log verification error; catches parsing or other issues
            log.error("Error verifying token", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    /**
     * Builds a scope string from a user's roles.
     *
     * @param user the {@link User} whose roles are to be converted to scope
     * @return a space-separated string of role names prefixed with "ROLE_" (e.g., "ROLE_USER ROLE_ADMIN")
     * @throws AppException if user is null (ErrorCode.USER_NOT_EXISTED)
     * @implNote Maps roles to "ROLE_<name>" format and joins with spaces; returns empty string if no roles.
     */
    private String buildScope(User user) {
        // Validate user; prevents null pointer issues
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        // Convert roles to scope; handles null roles gracefully
        return Optional.ofNullable(user.getRoles())
                .map(roles -> roles.stream().map(role -> "ROLE_" + role.getName()).toList()) // Prefix roles
                .map(roleList -> String.join(" ", roleList))                         // Join with spaces
                .orElse("");                                                         // Empty if no roles
    }
}