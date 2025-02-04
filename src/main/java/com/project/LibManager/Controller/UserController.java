package com.project.LibManager.Controller;

import com.project.LibManager.dto.request.RegisterRequest;
import com.project.LibManager.dto.response.ApiResponse;
import com.project.LibManager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    UserService userService;
    @PostMapping
    ApiResponse<String> registerUser(@RequestBody @Valid RegisterRequest registerRequest) {
        ApiResponse<String> apiRs = new ApiResponse<String>();
        apiRs.setMessage("Tao tai khoan thanh cong");
        apiRs.setResult(userService.createUser(registerRequest));
        return apiRs;
    }
}
