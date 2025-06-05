package com.project.libmanager.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    private static final String NAMEJWT = "JWT Authentication";
    private static final String MEDIA_TYPE = "application/json";
    private static final String LINK_SCHEMAS = "#/components/schemas/ApiResponseString";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // Add info about the OpenAPI version
                .info(new Info()
                        .title("Library Management API")
                        .version("1.0")
                        .description("API for managing library system")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                // Add require security check
                .addSecurityItem(new SecurityRequirement().addList(NAMEJWT))
                // Add component security scheme
                .components(new Components()
                        .addSecuritySchemes(NAMEJWT, new SecurityScheme()
                                .name(NAMEJWT)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> openApi.getPaths().forEach((path, pathItem) -> pathItem.readOperations().forEach(operation -> {
            ApiResponses apiResponses = operation.getResponses();

            // Add 400 Bad Request
            apiResponses.addApiResponse("400", new ApiResponse()
                    .description("Bad Request - Invalid input data")
                    .content(new Content()
                            .addMediaType(MEDIA_TYPE, new MediaType()
                                    .schema(new Schema<>().$ref(LINK_SCHEMAS))
                                    .example("{\"code\": 1003, \"message\": \"Invalid email address\", \"result\": null}"))));

            // Add 401 Unauthorized
            apiResponses.addApiResponse("401", new ApiResponse()
                    .description("Unauthorized - Invalid or missing JWT token")
                    .content(new Content()
                            .addMediaType(MEDIA_TYPE, new MediaType()
                                    .schema(new Schema<>().$ref(LINK_SCHEMAS))
                                    .example("{\"code\": 1006, \"message\": \"Unauthenticated\", \"result\": null}"))));

            // Add 403 Forbidden
            apiResponses.addApiResponse("403", new ApiResponse()
                    .description("Forbidden - Insufficient permissions")
                    .content(new Content()
                            .addMediaType(MEDIA_TYPE, new MediaType()
                                    .schema(new Schema<>().$ref(LINK_SCHEMAS))
                                    .example("{\"code\": 1007, \"message\": \"You do not have permission\", \"result\": null}"))));

            // Add 404 Not Found
            apiResponses.addApiResponse("404", new ApiResponse()
                    .description("Not Found - Resource not found")
                    .content(new Content()
                            .addMediaType(MEDIA_TYPE, new MediaType()
                                    .schema(new Schema<>().$ref(LINK_SCHEMAS))
                                    .example("{\"code\": 1005, \"message\": \"User not existed\", \"result\": null}"))));

            // Add 500 Internal Server Error
            apiResponses.addApiResponse("500", new ApiResponse()
                    .description("Internal Server Error - Unexpected error")
                    .content(new Content()
                            .addMediaType(MEDIA_TYPE, new MediaType()
                                    .schema(new Schema<>().$ref(LINK_SCHEMAS))
                                    .example("{\"code\": 9999, \"message\": \"Uncategorized error\", \"result\": null}"))));
        }));
    }
}