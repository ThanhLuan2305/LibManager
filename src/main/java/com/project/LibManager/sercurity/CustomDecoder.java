package com.project.LibManager.sercurity;

import java.util.Objects;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.project.LibManager.dto.request.TokenRequest;
import com.project.LibManager.service.IAuthenticationService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.oauth2.jwt.JwtDecoder;

@Component
@RequiredArgsConstructor
public class CustomDecoder implements JwtDecoder {
    @Value("${jwt.signing.key}")
    String signKey;

    private final IAuthenticationService authenticationService;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Override
    public Jwt decode(String token) throws JwtException {

        try {
            // check token is valid
            var response = authenticationService.introspectToken(
                    TokenRequest.builder().token(token).build());

            if (!response.isValid()) throw new BadJwtException("Token invalid");
        } catch (Exception e) {
            throw new BadJwtException(e.getMessage());
        }

        if (Objects.isNull(nimbusJwtDecoder)) {
            // check token từ header 
            SecretKeySpec secretKeySpec = new SecretKeySpec(signKey.getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }

        return nimbusJwtDecoder.decode(token);
    }
}
