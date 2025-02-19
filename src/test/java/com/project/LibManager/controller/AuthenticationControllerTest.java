package com.project.LibManager.controller;

import com.project.LibManager.dto.response.RoleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.project.LibManager.dto.request.ChangePasswordRequest;
import com.project.LibManager.dto.request.TokenRequest;
import com.project.LibManager.dto.request.UserCreateRequest;
import com.project.LibManager.dto.request.VerifyChangeMailRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.LibManager.constant.ErrorCode;
import com.project.LibManager.dto.request.AuthenticationRequest;
import com.project.LibManager.dto.request.ChangeMailRequest;
import com.project.LibManager.dto.response.AuthenticationResponse;
import com.project.LibManager.dto.response.IntrospectResponse;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.exception.AppException;
import com.project.LibManager.service.IAuthenticationService;

import java.time.LocalDate;
import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IAuthenticationService authenticationService;

    private AuthenticationResponse authenticationResponse;
    private IntrospectResponse introspectRequest;
    private UserResponse userResponse;

    private AuthenticationRequest authenticationRequest;
    private ChangeMailRequest changeMailRequest;
    private ChangePasswordRequest changePasswordRequest;
    private TokenRequest tokenRequest;
    private UserCreateRequest userCreateRequest;
    private VerifyChangeMailRequest verifyChangeEmail;
    private IntrospectResponse introspectResponse;

    @BeforeEach
    void initData() {
        authenticationRequest = AuthenticationRequest.builder()
                .email("testuser@example.com")
                .password("SecurePass123")
                .build();

        authenticationResponse = AuthenticationResponse.builder()
                .authenticate(true)
                .token("mock-jwt-token")
                .build();

        introspectRequest = IntrospectResponse.builder()
                .valid(true)
                .build();

        changeMailRequest = ChangeMailRequest.builder()
                .oldEmail("testuser@example.com")
                .newEmail("newuser@example.com")
                .build();

        changePasswordRequest = ChangePasswordRequest.builder()
                .oldPassword("SecurePass123")
                .newPassword("NewSecurePass456")
                .confirmPassword("NewSecurePass456")
                .build();

        tokenRequest = TokenRequest.builder()
                .token("mock-jwt-token")
                .build();

        userCreateRequest = UserCreateRequest.builder()
                .email("newuser@example.com")
                .password("SecurePass123")
                .fullName("New User")
                .birthDate(LocalDate.of(2000, 1, 1))
                .build();

        verifyChangeEmail = VerifyChangeMailRequest.builder()
                .oldEmail("testuser@example.com")
                .newEmail("newuser@example.com")
                .otp(123456)
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .email("testuser@example.com")
                .fullName("Test User")
                .birthDate(LocalDate.of(1998, 5, 20))
                .isVerified(true)
                .roles(Set.of(RoleResponse.builder().id(1L).name("USER").description("User role").build()))
                .build();

        tokenRequest = TokenRequest.builder()
                .token("valid-token")
                .build();

        introspectResponse = IntrospectResponse.builder()
                .valid(true)
                .build();
    }

    @Test
    void login_success() throws Exception {
        //GIVEN
        ObjectMapper object = new ObjectMapper();
        //object.registerModule(new JavaTimeModule());
        String content = object.writeValueAsString(authenticationRequest);

        Mockito.when(authenticationService.authenticate(authenticationRequest)).thenReturn(authenticationResponse);
        //WHEN, THEN

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(200)
        );
    }

    @Test
    void login_invalid_password() throws Exception {
        //GIVEN
        ObjectMapper object = new ObjectMapper();
        //object.registerModule(new JavaTimeModule());
        String content = object.writeValueAsString(authenticationRequest);

        Mockito.when(authenticationService.authenticate(authenticationRequest)).thenThrow(new AppException(ErrorCode.PASSWORD_NOT_MATCH));
        //WHEN, THEN

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(ErrorCode.PASSWORD_NOT_MATCH.getCode()))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("message")
                        .value(ErrorCode.PASSWORD_NOT_MATCH.getMessage()));
    }

    @Test
    void login_user_not_found() throws Exception {
        // GIVEN
        ObjectMapper object = new ObjectMapper();
        String content = object.writeValueAsString(authenticationRequest);

        Mockito.when(authenticationService.authenticate(authenticationRequest))
                .thenThrow(new AppException(ErrorCode.USER_NOT_EXISTED));

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("code")
                        .value(ErrorCode.USER_NOT_EXISTED.getCode()))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("message")
                        .value(ErrorCode.USER_NOT_EXISTED.getMessage()));
    }

    @Test
    void introspectToken_success() throws Exception {
        // GIVEN
        Mockito.when(authenticationService.introspectToken(ArgumentMatchers.any(TokenRequest.class)))
                .thenReturn(introspectResponse);

        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(tokenRequest);

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/introspect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.valid").value(true));
    }

    @Test
    void introspectToken_invalidRequest() throws Exception {
        // GIVEN
        TokenRequest invalidRequest = TokenRequest.builder().build(); // Empty request
        Mockito.when(authenticationService.introspectToken(ArgumentMatchers.any(TokenRequest.class)))
                .thenThrow(new AppException(ErrorCode.JWT_TOKEN_INVALID));

        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(invalidRequest);

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/introspect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ErrorCode.JWT_TOKEN_INVALID.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(ErrorCode.JWT_TOKEN_INVALID.getMessage()));
    }

    @Test
    void logout_success() throws Exception {
        // GIVEN
        TokenRequest logoutRequest = TokenRequest.builder()
                .token("valid-token")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(logoutRequest);

        Mockito.doNothing().when(authenticationService).logout(ArgumentMatchers.any(TokenRequest.class));

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Logout successfully"));
    }

    @Test
    void logout_invalidRequest() throws Exception {
        // GIVEN
        TokenRequest invalidRequest = TokenRequest.builder().build(); // Empty request
        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(invalidRequest);

        Mockito.doThrow(new AppException(ErrorCode.JWT_TOKEN_INVALID))
            .when(authenticationService).logout(ArgumentMatchers.any(TokenRequest.class));

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ErrorCode.JWT_TOKEN_INVALID.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(ErrorCode.JWT_TOKEN_INVALID.getMessage()));
    }

    @Test
    void refreshToken_success() throws Exception {
        // GIVEN
        TokenRequest refreshRequest = TokenRequest.builder()
                .token("valid-refresh-token")
                .build();

        AuthenticationResponse refreshedTokenResponse = AuthenticationResponse.builder()
                .authenticate(true)
                .token("new-access-token")
                .build();

        Mockito.when(authenticationService.refreshToken(ArgumentMatchers.any(TokenRequest.class)))
                .thenReturn(refreshedTokenResponse);

        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(refreshRequest);

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.authenticate").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.token").value("new-access-token"));
    }

    @Test
    void refreshToken_expiredToken() throws Exception {
        // GIVEN
        TokenRequest expiredTokenRequest = TokenRequest.builder()
                .token("expired-token")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(expiredTokenRequest);

        Mockito.when(authenticationService.refreshToken(ArgumentMatchers.any(TokenRequest.class)))
                .thenThrow(new AppException(ErrorCode.JWT_TOKEN_EXPIRED));

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ErrorCode.JWT_TOKEN_EXPIRED.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(ErrorCode.JWT_TOKEN_EXPIRED.getMessage()));
    }

    @Test
    void verifyEmail_success() throws Exception {
        // GIVEN
        String token = "valid-verification-token";
        Mockito.when(authenticationService.verifyEmail(token)).thenReturn(true);

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/auth/verify-email")
                        .param("token", token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Verify email successfully"));
    }

    @Test
    void verifyEmail_tokenInvalid() throws Exception {
        // GIVEN
        String invalidToken = "invalid-token";
        Mockito.when(authenticationService.verifyEmail(invalidToken))
                .thenThrow(new AppException(ErrorCode.JWT_TOKEN_INVALID));

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/auth/verify-email")
                        .param("token", invalidToken))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ErrorCode.JWT_TOKEN_INVALID.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(ErrorCode.JWT_TOKEN_INVALID.getMessage()));
    }

    @Test
    void register_success() throws Exception {
        // GIVEN

        Mockito.when(authenticationService.registerUser(ArgumentMatchers.any(UserCreateRequest.class)))
                .thenReturn(userResponse);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(userCreateRequest);

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void register_missingRequiredFields() throws Exception {
        // GIVEN
        UserCreateRequest invalidRequest = UserCreateRequest.builder()
                .email("newuser@example.com")
                // Missing required fields: password, fullName, birthDate
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(invalidRequest);

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ErrorCode.NOT_BLANK.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value(ErrorCode.NOT_BLANK.getMessage()));
    }

    @WithMockUser(username = "testuser@example.com", roles = {"USER"})
    @Test
    void changePassword_success() throws Exception {
        // GIVEN
        Mockito.when(authenticationService.changePassword(ArgumentMatchers.any(ChangePasswordRequest.class)))
                .thenReturn(true);

        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(changePasswordRequest);

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("result").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("Change password successfully"));
    }

    @WithMockUser(username = "testuser@example.com", roles = {"USER"})
    @Test
    void changePassword_newPasswordSameAsOld() throws Exception {
        // GIVEN
        ChangePasswordRequest samePasswordRequest = ChangePasswordRequest.builder()
                .oldPassword("oldPassword123")
                .newPassword("oldPassword123")
                .confirmPassword("oldPassword123")
                .build();

        Mockito.when(authenticationService.changePassword(ArgumentMatchers.any(ChangePasswordRequest.class)))
                .thenThrow(new AppException(ErrorCode.PASSWORD_DUPLICATED));

        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(samePasswordRequest);

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ErrorCode.PASSWORD_DUPLICATED.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(ErrorCode.PASSWORD_DUPLICATED.getMessage()));
    }

    @Test
    void forgetPassword_success() throws Exception {
        // GIVEN
        String email = "test@example.com";

        Mockito.doNothing().when(authenticationService).forgetPassword(email);

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/forget-password")
                        .param("email", email))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("Please check your email to reset password"))
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("success"));
    }

    @Test
    void forgetPassword_emailNotFound() throws Exception {
        // GIVEN
        String nonExistentEmail = "nonexistent@example.com";
        Mockito.doThrow(new AppException(ErrorCode.USER_NOT_EXISTED))
                .when(authenticationService).forgetPassword(nonExistentEmail);

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/forget-password")
                        .param("email", nonExistentEmail))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ErrorCode.USER_NOT_EXISTED.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(ErrorCode.USER_NOT_EXISTED.getMessage()));
    }

    @Test
    void verifyOtp_success() throws Exception {
        // GIVEN
        Integer otp = 123456;
        String email = "testuser@example.com";
        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .authenticate(true)
                .token("new-token-after-otp-verification")
                .build();

        Mockito.when(authenticationService.verifyOTP(otp, email)).thenReturn(authResponse);

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/verify-otp")
                        .param("otp", otp.toString())
                        .param("email", email))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Verify OTP successfully"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.authenticate").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.token").value("new-token-after-otp-verification"));
    }

    @Test
    void verifyOtp_emailNotFound() throws Exception {
        // GIVEN
        Integer otp = 123456;
        String email = "nonexistent@example.com";

        Mockito.when(authenticationService.verifyOTP(otp, email))
                .thenThrow(new AppException(ErrorCode.USER_NOT_EXISTED));

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/verify-otp")
                        .param("otp", otp.toString())
                        .param("email", email))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ErrorCode.USER_NOT_EXISTED.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(ErrorCode.USER_NOT_EXISTED.getMessage()));
    }

    @Test
    void resetPassword_success() throws Exception {
        // GIVEN
        TokenRequest tokenRequest = TokenRequest.builder()
                .token("reset-password-token")
                .build();

        String newPassword = "newPassword123";
        Mockito.when(authenticationService.resetPassword(tokenRequest.getToken())).thenReturn(newPassword);

        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(tokenRequest);

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Reset password successfully, you can login with new password"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").value(newPassword));
    }

    @Test
    void resetPassword_expiredToken() throws Exception {
        // GIVEN
        TokenRequest expiredTokenRequest = TokenRequest.builder()
                .token("expired-token")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(expiredTokenRequest);

        Mockito.when(authenticationService.resetPassword(ArgumentMatchers.anyString()))
                .thenThrow(new AppException(ErrorCode.JWT_TOKEN_EXPIRED));

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ErrorCode.JWT_TOKEN_EXPIRED.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(ErrorCode.JWT_TOKEN_EXPIRED.getMessage()));
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"USER"})
    void changeMail_success() throws Exception {
        // GIVEN
        ChangeMailRequest changeMailRequest = ChangeMailRequest.builder()
                .oldEmail("olduser@example.com")
                .newEmail("newuser@example.com")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(changeMailRequest);

        Mockito.doNothing().when(authenticationService).changeEmail(ArgumentMatchers.any(ChangeMailRequest.class));

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/change-mail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("Please verify your new email to change new email"))
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("success"));
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"USER"})
    void changeMail_newEmailAlreadyExists() throws Exception {
        // GIVEN
        ChangeMailRequest changeMailRequest = ChangeMailRequest.builder()
                .oldEmail("existing@example.com")
                .newEmail("alreadyexists@example.com")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(changeMailRequest);

        Mockito.doThrow(new AppException(ErrorCode.USER_EXISTED))
                .when(authenticationService).changeEmail(ArgumentMatchers.any(ChangeMailRequest.class));

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/change-mail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(ErrorCode.USER_EXISTED.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value(ErrorCode.USER_EXISTED.getMessage()));
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"USER"})
    void verifyChangeMail_success() throws Exception {
        // GIVEN
        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(verifyChangeEmail);

        Mockito.doNothing().when(authenticationService).verifyChangeEmail(ArgumentMatchers.any(VerifyChangeMailRequest.class));

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/verify-change-mail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("Change email successfully, you can login with new email"))
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("success"));
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"USER"})
    void verifyChangeMail_invalidOTP() throws Exception {
        // GIVEN
        VerifyChangeMailRequest invalidRequest = VerifyChangeMailRequest.builder()
                .oldEmail("testuser@example.com")
                .newEmail("newuser@example.com")
                .otp(999999) // Invalid OTP
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(invalidRequest);

        Mockito.doThrow(new AppException(ErrorCode.OTP_NOT_EXISTED))
                .when(authenticationService).verifyChangeEmail(ArgumentMatchers.any(VerifyChangeMailRequest.class));

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/verify-change-mail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ErrorCode.OTP_NOT_EXISTED.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(ErrorCode.OTP_NOT_EXISTED.getMessage()));
    }
}
