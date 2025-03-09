package com.project.libmanager.sercurity;

import java.text.ParseException;
import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.libmanager.entity.LoginDetail;
import com.project.libmanager.repository.LoginDetailRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomDecoder implements JwtDecoder {
    @Value("${jwt.signing.key}")
    private String signKey;

    private final LoginDetailRepository loginDetailRepository;
    private NimbusJwtDecoder nimbusJwtDecoder;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            // Kiểm tra token có hợp lệ không
            if (!signedJWT.verify(new MACVerifier(signKey.getBytes()))) {
                log.error("Invalid token signature");
                throw new BadJwtException("Invalid token signature");
            }

            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            String jwtID = claimsSet.getJWTID();

            // Kiểm tra token có tồn tại trong database không
            LoginDetail loginDetail = loginDetailRepository.findByJti(jwtID)
                    .orElseThrow(() -> new BadJwtException("User does not exist"));

            if (!loginDetail.isEnabled()) {
                log.error("Account is logged out!");
                throw new BadJwtException("Token is invalid because the user is logged out");
            }

            // Khởi tạo decoder nếu chưa có
            if (Objects.isNull(nimbusJwtDecoder)) {
                SecretKeySpec secretKeySpec = new SecretKeySpec(signKey.getBytes(), "HmacSHA512");
                nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                        .macAlgorithm(MacAlgorithm.HS512)
                        .build();
            }

            return nimbusJwtDecoder.decode(token);
        } catch (ParseException e) {
            log.error("Error parsing token claims", e);
            throw new BadJwtException("Error parsing token claims");
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error decoding JWT", e);
            throw new BadJwtException("Unexpected error decoding JWT");
        }
    }
}
