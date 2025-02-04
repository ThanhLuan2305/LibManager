package com.project.LibManager.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;
import lombok.experimental.FieldDefaults;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ApiResponse<T> {
	 private int code = 200;
	 private String message;
	 private T result;
}
