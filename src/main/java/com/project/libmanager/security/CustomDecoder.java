package com.project.libmanager.security;

import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.libmanager.constant.TokenType;
import com.project.libmanager.entity.LoginDetail;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.repository.LoginDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

/**
 * Custom implementation of Spring Security's {@link JwtDecoder} for decoding and validating JWT tokens.
 * Extends default decoding with additional checks for token type and login session status.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomDecoder implements JwtDecoder {
    @Value("${jwt.signing.key}")
    private String signKey;                         // Secret key for verifying token signatures

    private final LoginDetailRepository loginDetailRepository; // Repository for login session data
    private NimbusJwtDecoder nimbusJwtDecoder;      // Decoder instance, lazily initialized

    /**
     * Decodes and validates a JWT token, ensuring it is an access token and tied to an active session.
     *
     * @param token the JWT token string to decode
     * @return a {@link Jwt} object containing validated token claims
     * @throws JwtException if decoding or validation fails, including:
     *                      - {@link BadJwtException} for invalid signature, expiration, type, or session
     *                      - {@link AppException} propagated from repository lookup
     * @implNote Verifies signature, expiration, token type, and session status before delegating to
     * {@link NimbusJwtDecoder} for final decoding.
     */
    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            // Parse token; converts string to JWT object
            SignedJWT signedJWT = SignedJWT.parse(token);

            // Verify signature; uses HMAC with secret key
            if (!signedJWT.verify(new MACVerifier(signKey.getBytes()))) {
                log.error("Invalid token signature");
                throw new BadJwtException("Invalid token signature");
            }

            // Extract claims; contains token metadata
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            String jwtID = claimsSet.getJWTID();         // Unique token identifier
            String typeToken = claimsSet.getStringClaim("type"); // Token type

            // Check expiration; ensures token is still valid
            if (claimsSet.getExpirationTime() == null || claimsSet.getExpirationTime().before(new Date())) {
                log.error("Token has expired");
                throw new BadJwtException("Token has expired");
            }

            // Validate token type; restricts to access tokens only
            if (!TokenType.ACCESS.name().equals(typeToken)) {
                log.error("Token is not access token");
                throw new BadJwtException("Token is invalid because not an access token");
            }

            // Check login session; ensures token is tied to an active session
            LoginDetail loginDetail = loginDetailRepository.findByJti(jwtID)
                    .orElseThrow(() -> new BadJwtException("User does not exist"));
            if (!loginDetail.isEnabled()) {
                log.error("Account is logged out!");
                throw new BadJwtException("Token is invalid because the user is logged out");
            }

            // Initialize decoder lazily; reuses instance for efficiency
            if (Objects.isNull(nimbusJwtDecoder)) {
                // Create secret key spec; aligns with HS512 algorithm
                SecretKeySpec secretKeySpec = new SecretKeySpec(signKey.getBytes(), "HmacSHA512");
                // Build decoder; configures Nimbus with secret key and algorithm
                nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                        .macAlgorithm(MacAlgorithm.HS512)
                        .build();
            }

            // Delegate decoding; returns validated Jwt object
            return nimbusJwtDecoder.decode(token);
        } catch (ParseException e) {
            // Log parsing error; indicates malformed token
            log.error("Error parsing token claims: {}", e);
            throw new BadJwtException("Error parsing token claims");
        } catch (JwtException | AppException e) {
            log.error("Error parsing token claims: {}", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error decoding JWT: {}", e);
            throw new BadJwtException("Unexpected error decoding JWT");
        }
    }
}