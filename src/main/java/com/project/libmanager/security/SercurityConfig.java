package com.project.libmanager.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SercurityConfig {
    @Value("#{'${security.public-endpoints-post}'.split(',')}")
    private String[] publicEndPoints;

    @Value("#{'${security.public-endpoints-get}'.split(',')}")
    private String[] publicEndPointsGet;

    @Value("#{'${security.permissions.admin_role}'.split(',')}")
    private String[] adminEndPoint;

    @Value("#{'${security.permissions.user_role}'.split(',')}")
    private String[] userEndPoint;

    private final CustomDecoder customDecoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        httpSecurity.authorizeHttpRequests(request -> request
                .requestMatchers(HttpMethod.POST, publicEndPoints).permitAll()
                .requestMatchers(HttpMethod.GET, publicEndPointsGet).permitAll()
                .requestMatchers(adminEndPoint).hasAnyAuthority("ROLE_ADMIN")
                .requestMatchers(userEndPoint).hasAnyAuthority("ROLE_USER")
                .anyRequest()
                .permitAll());
        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwtConfigure -> jwtConfigure
                        .decoder(customDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .accessDeniedHandler(new CustomAccessDeniedHandler())
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));

        httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        return httpSecurity.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

}
