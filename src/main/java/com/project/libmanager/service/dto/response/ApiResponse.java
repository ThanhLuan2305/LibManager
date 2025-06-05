package com.project.libmanager.service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic response wrapper for API responses.
 *
 * @param <T> the type of the result data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Generic response wrapper for API responses")
public class ApiResponse<T> {
    @Builder.Default
    @Schema(description = "Response code", example = "200")
    private int code = 200;

    @Schema(description = "Response message", example = "Success action!!!")
    private String message;

    @Schema(description = "Result data of the response")
    private T result;
}