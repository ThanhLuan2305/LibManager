package com.project.LibManager.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
        private static final String NAMEJWT = "JWT Authentication";

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Library Management API")
                                                .version("1.0")
                                                .description("API for managing library system")
                                                .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                                .addSecurityItem(new SecurityRequirement().addList("J"))
                                .components(new Components().addSecuritySchemes(NAMEJWT,
                                                new SecurityScheme()
                                                                .name(NAMEJWT)
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")));
        }
}
