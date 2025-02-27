package com.project.LibManager.sercurity;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.project.LibManager.config.CustomAccessDeniedHandler;
import com.project.LibManager.config.CustomDecoder;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SercurityConfig {
    @Value("#{'${security.public-endpoints-post}'.split(',')}")
    private String[] PUBLIC_ENDPOINTS;

    @Value("#{'${security.public-endpoints-get}'.split(',')}")
    private String[] PUBLIC_ENDPOINTS_GET;
    
    @Value("#{'${security.permissions.admin_role}'.split(',')}")
    private String[] ADMIN_ENDPOINT;

    @Value("#{'${security.permissions.user_role}'.split(',')}")
    private String[] USER_ENDPOINT;

    @Autowired
    CustomDecoder customDecoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(request -> request
                .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers(HttpMethod.GET, PUBLIC_ENDPOINTS_GET).permitAll()
                .requestMatchers(ADMIN_ENDPOINT).hasAnyAuthority("ROLE_ADMIN")
                .requestMatchers(USER_ENDPOINT).hasAnyAuthority("ROLE_USER")
                .anyRequest()
                .permitAll());
        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2
                            .jwt(jwtConfigure -> jwtConfigure
                                                    .decoder(customDecoder)
                                                    .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                            .accessDeniedHandler(new CustomAccessDeniedHandler())
                            .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
        );
        httpSecurity.csrf(csrf -> csrf.disable());
        return httpSecurity.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }
}
