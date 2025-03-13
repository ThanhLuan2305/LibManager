// package com.project.libmanager.service;

// import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyBoolean;
// import static org.mockito.ArgumentMatchers.anyInt;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.*;

// import java.text.ParseException;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.Collections;
// import java.util.Date;
// import java.util.HashSet;
// import java.util.Optional;
// import java.util.Set;

// import com.nimbusds.jose.JOSEException;
// import com.nimbusds.jose.JWSHeader;
// import com.nimbusds.jose.JWSObject;
// import com.nimbusds.jose.Payload;
// import com.nimbusds.jose.crypto.MACSigner;
// import com.nimbusds.jwt.JWTClaimsSet;
// import com.nimbusds.jwt.SignedJWT;
// import com.project.libmanager.constant.ErrorCode;
// import com.project.libmanager.constant.PredefinedRole;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContext;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.test.context.TestPropertySource;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;
// import org.springframework.test.util.ReflectionTestUtils;

// import com.project.libmanager.dto.request.AuthenticationRequest;
// import com.project.libmanager.dto.request.ChangeMailRequest;
// import com.project.libmanager.dto.request.ChangePasswordRequest;
// import com.project.libmanager.dto.request.TokenRequest;
// import com.project.libmanager.dto.request.UserCreateRequest;
// import com.project.libmanager.dto.request.VerifyChangeMailRequest;
// import com.project.libmanager.dto.response.AuthenticationResponse;
// import com.project.libmanager.dto.response.IntrospectResponse;
// import com.project.libmanager.dto.response.RoleResponse;
// import com.project.libmanager.dto.response.UserResponse;
// import com.project.libmanager.repository.InvalidateTokenRepository;
// import com.project.libmanager.repository.OtpVerificationRepository;
// import com.project.libmanager.repository.RoleRepository;
// import com.project.libmanager.repository.UserRepository;
// import com.project.libmanager.service.impl.AuthenticationServiceImpl;
// import com.project.libmanager.entity.InvalidateToken;
// import com.project.libmanager.entity.OtpVerification;
// import com.project.libmanager.entity.Role;
// import com.project.libmanager.entity.User;
// import com.project.libmanager.exception.AppException;

// import lombok.extern.slf4j.Slf4j;

// @Slf4j
// @SpringBootTest
// @TestPropertySource("/test.properties")
// public class AuthenticationServiceTest {

//     @InjectMocks
//     private AuthenticationServiceImpl authenticationService;

//     @MockitoBean
//     private UserRepository userRepository;
//     @MockitoBean
//     private IUserService userService;
//     @MockitoBean
//     private InvalidateTokenRepository invalidateTokenRepository;
//     @MockitoBean
//     private IMailService mailService;
//     @MockitoBean
//     private OtpVerificationRepository otpRepository;
//     @MockitoBean
//     private PasswordEncoder passwordEncoder;
//     @MockitoBean
//     private IMaintenanceService maintenanceService;
//     @MockitoBean
//     private RoleRepository roleRepository;
    

//     @Mock
//     private SignedJWT signedJWT;

//     @Mock
//     private SecurityContext securityContext;

//     @Mock
//     private Authentication authentication;

//     @Mock
//     private JWSHeader jwsHeader;

//     @Mock
//     private JWTClaimsSet jwtClaimsSet;

//     @Mock
//     private Payload payload;

//     @Mock
//     private JWSObject jwsObject;

//     @Mock
//     private MACSigner macSigner;
    
//     private AuthenticationResponse authenticationResponse;
//     private IntrospectResponse introspectRequest;
//     private UserResponse userResponse;

//     private AuthenticationRequest authenticationRequest;
//     private ChangeMailRequest changeMailRequest;
//     private ChangePasswordRequest changePasswordRequest;
//     private TokenRequest tokenRequest;
//     private UserCreateRequest userCreateRequest;
//     private VerifyChangeMailRequest verifyChangeEmail;
//     private IntrospectResponse introspectResponse;

//     private InvalidateToken invalidateToken;
//     private OtpVerification otpVerification;
//     private Role role;
//     private User user;


//     @BeforeEach
//     void initData() throws ParseException {
//         authenticationRequest = AuthenticationRequest.builder()
//                 .email("testuser@example.com")
//                 .password("SecurePass123")
//                 .build();

//         authenticationResponse = AuthenticationResponse.builder()
//                 .authenticate(true)
//                 .token("mock-jwt-token")
//                 .build();

//         introspectRequest = IntrospectResponse.builder()
//                 .valid(true)
//                 .build();

//         changeMailRequest = ChangeMailRequest.builder()
//                 .oldEmail("testuser@example.com")
//                 .newEmail("newuser@example.com")
//                 .build();

//         changePasswordRequest = ChangePasswordRequest.builder()
//                 .oldPassword("SecurePass123")
//                 .newPassword("NewSecurePass456")
//                 .confirmPassword("NewSecurePass456")
//                 .build();

//         tokenRequest = TokenRequest.builder()
//                 .token("mock-jwt-token")
//                 .build();

//         userCreateRequest = UserCreateRequest.builder()
//                 .email("newuser@example.com")
//                 .password("SecurePass123")
//                 .fullName("New User")
//                 .birthDate(LocalDate.of(2000, 1, 1))
//                 .build();

//         verifyChangeEmail = VerifyChangeMailRequest.builder()
//                 .oldEmail("testuser@example.com")
//                 .newEmail("newuser@example.com")
//                 .otp(123456)
//                 .build();

//         userResponse = UserResponse.builder()
//                 .id(1L)
//                 .email("testuser@example.com")
//                 .fullName("Test User")
//                 .birthDate(LocalDate.of(1998, 5, 20))
//                 .isVerified(true)
//                 .roles(Set.of(RoleResponse.builder().id(1L).name("USER").description("User role").build()))
//                 .build();

//         tokenRequest = TokenRequest.builder()
//                 .token("valid-token")
//                 .build();

//         introspectResponse = IntrospectResponse.builder()
//                 .valid(true)
//                 .build();

//         invalidateToken = InvalidateToken.builder()
//             .id("token-123")
//             .expiryTime(new Date(System.currentTimeMillis() + 3600000))
//             .build();

//         otpVerification = OtpVerification.builder()
//                 .id(1L)
//                 .email("testuser@example.com")
//                 .otp(123456)
//                 .expiredAt(LocalDateTime.now().plusMinutes(10))
//                 .build();

//         role = Role.builder()
//                 .id(1L)
//                 .name("USER")
//                 .description("User role")
//                 .build();

//         user = User.builder()
//                 .id(1L)
//                 .email("testuser@example.com")
//                 .password("SecurePass123")
//                 .isVerified(true)
//                 .fullName("Test User")
//                 .birthDate(LocalDate.of(1998, 5, 20))
//                 .roles(Set.of(role))
//                 .isDeleted(false)
//             .build();

//         SecurityContextHolder.setContext(securityContext);
//         when(securityContext.getAuthentication()).thenReturn(authentication);
//     }

//     @Test
//     void authenticate_shouldThrowAppException_whenUserNotFound() {
//         // Arrange
//         when(userRepository.findByEmail(authenticationRequest.getEmail())).thenReturn(null);

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.authenticate(authenticationRequest));
//         assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
//     }

//     @Test
//     void shouldThrowAppExceptionWhenEmailIsNotVerified() {
//         // Arrange
//         user.setIsVerified(false);

//         when(userRepository.findByEmail(authenticationRequest.getEmail())).thenReturn(Optional.of(user));


//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.authenticate(authenticationRequest));
//         assertEquals(ErrorCode.EMAIL_NOT_VERIFIED, exception.getErrorCode());
//     }

//     @Test
//     void authenticate_shouldThrowAppException_whenUserIsMarkedAsDeleted() {
        
//         user.setIsVerified(true);
//         user.setIsDeleted(true);

//         when(userRepository.findByEmail(authenticationRequest.getEmail())).thenReturn(Optional.of(user));

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.authenticate(authenticationRequest));
//         assertEquals(ErrorCode.USER_IS_DELETED, exception.getErrorCode());
//     }

//     @Test
//     void authenticate_shouldThrowAppException_whenUserRoleNotFound() {
//         // Arrange
//         user.setIsDeleted(false);

//         when(userRepository.findByEmail(authenticationRequest.getEmail())).thenReturn(Optional.of(user));
//         when(roleRepository.findByName(PredefinedRole.USER_ROLE)).thenReturn(Optional.empty());

//         // Act & Assert
//         assertThrows(AppException.class, () -> authenticationService.authenticate(authenticationRequest));
//         verify(roleRepository).findByName(PredefinedRole.USER_ROLE);
//     }

//     @Test
//     void shouldThrowMaintenanceModeExceptionWhenSystemInMaintenanceAndUserHasUserRole() {
//         // Arrange

//         when(userRepository.findByEmail(authenticationRequest.getEmail())).thenReturn(Optional.of(user));
//         when(roleRepository.findByName(PredefinedRole.USER_ROLE)).thenReturn(Optional.of(role));
//         when(maintenanceService.isMaintenanceMode()).thenReturn(true);

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.authenticate(authenticationRequest));
//         assertEquals(ErrorCode.MAINTENACE_MODE, exception.getErrorCode());
//     }

//     @Test
//     void authenticate_shouldThrowAppException_whenPasswordDoesNotMatch() {
//         // Arrange

//         when(userRepository.findByEmail(authenticationRequest.getEmail())).thenReturn(Optional.of(user));
//         when(passwordEncoder.matches("wrongPassword", user.getPassword())).thenReturn(false);
//         when(roleRepository.findByName(PredefinedRole.USER_ROLE)).thenReturn(Optional.of(role));

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.authenticate(authenticationRequest));
//         assertEquals(ErrorCode.PASSWORD_NOT_MATCH, exception.getErrorCode());
//     }

//     @Test
//     void shouldAuthenticateSuccessfully() {
//         // Arrange
//         when(userRepository.findByEmail(authenticationRequest.getEmail())).thenReturn(Optional.of(user));
//         when(roleRepository.findByName(PredefinedRole.USER_ROLE)).thenReturn(Optional.of(new Role()));
//         when(maintenanceService.isMaintenanceMode()).thenReturn(false);
//         when(passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword())).thenReturn(true);

//         String mockToken = "mockToken";
//         // Set the fields using ReflectionTestUtils
//         ReflectionTestUtils.setField(authenticationService, "SIGN_KEY", "testSignKey");
//         ReflectionTestUtils.setField(authenticationService, "VALID_DURATION", 3600L);
//         ReflectionTestUtils.setField(authenticationService, "REFRESH_DURATION", 7200L);
//         ReflectionTestUtils.setField(authenticationService, "MAIL_DURATION", 1800L);
//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);
//         doReturn(mockToken).when(spyAuthenticationService).generateToken(any(User.class), anyBoolean());

//         // Act
//         AuthenticationResponse response = spyAuthenticationService.authenticate(authenticationRequest);

//         // Assert
//         assertTrue(response.isAuthenticate());
//         assertEquals(mockToken, response.getToken());
//         verify(userRepository).findByEmail(authenticationRequest.getEmail());
//         verify(passwordEncoder).matches(authenticationRequest.getPassword(), user.getPassword());
//         verify(spyAuthenticationService).generateToken(user, false);
//     }

//     @Test
//     void shouldIntrospectTokenSuccessfully() throws JOSEException, ParseException {
//         // Arrange
//         String mockToken = "valid-token";
//         TokenRequest tokenRequest = new TokenRequest(mockToken);

//         ReflectionTestUtils.setField(authenticationService, "SIGN_KEY", "testSignKey");

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);

//         SignedJWT mockSignedJWT = mock(SignedJWT.class);
//         doReturn(mockSignedJWT).when(spyAuthenticationService).verifyToken(mockToken, false);

//         // Act
//         IntrospectResponse response = spyAuthenticationService.introspectToken(tokenRequest);

//         // Assert
//         assertNotNull(response);
//         assertTrue(response.isValid());

//         verify(spyAuthenticationService).verifyToken(mockToken, false);
//     }

//     @Test
//     void shouldThrowAppExceptionWhenTokenIsInvalid() throws JOSEException, ParseException {
//         // Arrange
//         String invalidToken = "invalid-token";
//         TokenRequest tokenRequest = new TokenRequest(invalidToken);

//         ReflectionTestUtils.setField(authenticationService, "SIGN_KEY", "testSignKey");

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);

//         doThrow(new AppException(ErrorCode.JWT_TOKEN_INVALID))
//             .when(spyAuthenticationService).verifyToken(invalidToken, false);

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> 
//             spyAuthenticationService.introspectToken(tokenRequest)
//         );

//         assertEquals(ErrorCode.JWT_TOKEN_INVALID, exception.getErrorCode());

//         verify(spyAuthenticationService).verifyToken(invalidToken, false);
//     }

//     @Test
//     void shouldThrowAppExceptionWhenVerifyTokenThrowsException() throws JOSEException, ParseException {
//         // Arrange
//         String token = "some-token";
//         TokenRequest tokenRequest = new TokenRequest(token);

//         ReflectionTestUtils.setField(authenticationService, "SIGN_KEY", "testSignKey");

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);

//         doThrow(new JOSEException("Invalid signature"))
//             .when(spyAuthenticationService).verifyToken(token, false);

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> 
//             spyAuthenticationService.introspectToken(tokenRequest)
//         );

//         assertEquals(ErrorCode.JWT_TOKEN_INVALID, exception.getErrorCode());

//         verify(spyAuthenticationService).verifyToken(token, false);
//     }

//     @Test
//     void shouldThrowAppExceptionWhenVerifyTokenThrowsParseException() throws JOSEException, ParseException {
//         // Arrange
//         String token = "some-token";
//         TokenRequest tokenRequest = new TokenRequest(token);

//         ReflectionTestUtils.setField(authenticationService, "SIGN_KEY", "testSignKey");

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);

//         doThrow(new ParseException("Invalid token format", 0))
//             .when(spyAuthenticationService).verifyToken(token, false);

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> 
//             spyAuthenticationService.introspectToken(tokenRequest)
//         );

//         assertEquals(ErrorCode.JWT_TOKEN_INVALID, exception.getErrorCode());

//         verify(spyAuthenticationService).verifyToken(token, false);
//     }

//     @Test
//     void shouldLogoutSuccessfully() throws ParseException, JOSEException {
//         // Arrange
//         String mockToken = "valid-token";
//         TokenRequest tokenRequest = new TokenRequest(mockToken);

//         SignedJWT mockSignedJWT = mock(SignedJWT.class);
//         JWTClaimsSet mockClaimsSet = mock(JWTClaimsSet.class);

//         when(mockSignedJWT.getJWTClaimsSet()).thenReturn(mockClaimsSet);
//         when(mockClaimsSet.getJWTID()).thenReturn("jwt-id-123");
//         when(mockClaimsSet.getExpirationTime()).thenReturn(new Date(System.currentTimeMillis() + 3600000));

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);
//         doReturn(mockSignedJWT).when(spyAuthenticationService).verifyToken(mockToken, false);

//         // Act
//         spyAuthenticationService.logout(tokenRequest);

//         // Assert
//         verify(invalidateTokenRepository).save(any(InvalidateToken.class));
//         verify(spyAuthenticationService).verifyToken(mockToken, false);
//     }

//     @Test
//     void shouldNotThrowExceptionWhenTokenIsExpired() throws ParseException, JOSEException {
//         // Arrange
//         String expiredToken = "expired-token";
//         TokenRequest tokenRequest = new TokenRequest(expiredToken);

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);
        
//         // Giả lập verifyToken ném lỗi JWT_TOKEN_EXPIRED
//         doThrow(new AppException(ErrorCode.JWT_TOKEN_EXPIRED))
//             .when(spyAuthenticationService).verifyToken(expiredToken, false);

//         // Act & Assert (Không ném exception, chỉ log lỗi)
//         assertDoesNotThrow(() -> spyAuthenticationService.logout(tokenRequest));

//         verify(spyAuthenticationService).verifyToken(expiredToken, false);
//         verifyNoInteractions(invalidateTokenRepository); // Không lưu token vào DB
//     }

//     @Test
//     void shouldHandleExceptionWhenVerifyTokenThrowsParseException() throws ParseException, JOSEException {
//         // Arrange
//         String invalidToken = "invalid-token";
//         TokenRequest tokenRequest = new TokenRequest(invalidToken);

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);

//         // Giả lập verifyToken ném lỗi ParseException
//         doThrow(new ParseException("Invalid token format", 0))
//             .when(spyAuthenticationService).verifyToken(invalidToken, false);

//         // Act & Assert (Ném lỗi ParseException)
//         assertThrows(ParseException.class, () -> spyAuthenticationService.logout(tokenRequest));

//         verify(spyAuthenticationService).verifyToken(invalidToken, false);
//         verifyNoInteractions(invalidateTokenRepository);
//     }

//     @Test
//     void shouldHandleExceptionWhenVerifyTokenThrowsJOSEException() throws ParseException, JOSEException {
//         // Arrange
//         String invalidToken = "malformed-token";
//         TokenRequest tokenRequest = new TokenRequest(invalidToken);

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);

//         // Giả lập verifyToken ném lỗi JOSEException
//         doThrow(new JOSEException("Invalid signature"))
//             .when(spyAuthenticationService).verifyToken(invalidToken, false);

//         // Act & Assert (Ném lỗi JOSEException)
//         assertThrows(JOSEException.class, () -> spyAuthenticationService.logout(tokenRequest));

//         verify(spyAuthenticationService).verifyToken(invalidToken, false);
//         verifyNoInteractions(invalidateTokenRepository);
//     }

//     @Test
//     void shouldRefreshTokenSuccessfully() throws ParseException, JOSEException {
//         // Arrange
//         String refreshToken = "valid-refresh-token";
//         TokenRequest tokenRequest = new TokenRequest(refreshToken);

//         SignedJWT mockSignedJWT = mock(SignedJWT.class);
//         JWTClaimsSet mockClaimsSet = mock(JWTClaimsSet.class);
//         User mockUser = new User();
        
//         when(mockSignedJWT.getJWTClaimsSet()).thenReturn(mockClaimsSet);
//         when(mockClaimsSet.getJWTID()).thenReturn("jwt-id-123");
//         when(mockClaimsSet.getExpirationTime()).thenReturn(new Date(System.currentTimeMillis() + 3600000));
//         when(mockClaimsSet.getSubject()).thenReturn("user@example.com");

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);
//         doReturn(mockSignedJWT).when(spyAuthenticationService).verifyToken(refreshToken, true);
//         when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
//         doReturn("new-access-token").when(spyAuthenticationService).generateToken(mockUser, false);

//         // Act
//         AuthenticationResponse response = spyAuthenticationService.refreshToken(tokenRequest);

//         // Assert
//         assertNotNull(response);
//         assertTrue(response.isAuthenticate());
//         assertEquals("new-access-token", response.getToken());

//         verify(spyAuthenticationService).verifyToken(refreshToken, true);
//         verify(userRepository).findByEmail("user@example.com");
//         verify(invalidateTokenRepository).save(any(InvalidateToken.class));
//         verify(spyAuthenticationService).generateToken(mockUser, false);
//     }

//     @Test
//     void shouldThrowExceptionWhenUserDoesNotExist() throws ParseException, JOSEException {
//         // Arrange
//         String refreshToken = "valid-refresh-token";
//         TokenRequest tokenRequest = new TokenRequest(refreshToken);

//         SignedJWT mockSignedJWT = mock(SignedJWT.class);
//         JWTClaimsSet mockClaimsSet = mock(JWTClaimsSet.class);
        
//         when(mockSignedJWT.getJWTClaimsSet()).thenReturn(mockClaimsSet);
//         when(mockClaimsSet.getJWTID()).thenReturn("jwt-id-123");
//         when(mockClaimsSet.getExpirationTime()).thenReturn(new Date(System.currentTimeMillis() + 3600000));
//         when(mockClaimsSet.getSubject()).thenReturn("nonexistent@example.com");

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);
//         doReturn(mockSignedJWT).when(spyAuthenticationService).verifyToken(refreshToken, true);
//         when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(null);

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> spyAuthenticationService.refreshToken(tokenRequest));
//         assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());

//         verify(spyAuthenticationService).verifyToken(refreshToken, true);
//         verify(userRepository).findByEmail("nonexistent@example.com");
//         verify(spyAuthenticationService, never()).generateToken(any(User.class), anyBoolean());
//     }

//     @Test
//     void shouldThrowExceptionWhenTokenIsExpired() throws ParseException, JOSEException {
//         // Arrange
//         String expiredToken = "expired-refresh-token";
//         TokenRequest tokenRequest = new TokenRequest(expiredToken);

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);
//         doThrow(new AppException(ErrorCode.JWT_TOKEN_EXPIRED))
//             .when(spyAuthenticationService).verifyToken(expiredToken, true);

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> spyAuthenticationService.refreshToken(tokenRequest));
//         assertEquals(ErrorCode.JWT_TOKEN_EXPIRED, exception.getErrorCode());

//         verify(spyAuthenticationService).verifyToken(expiredToken, true);
//         verifyNoInteractions(userRepository);
//         verifyNoInteractions(invalidateTokenRepository);
//         verify(spyAuthenticationService, never()).generateToken(any(User.class), anyBoolean());
//     }

//     @Test
//     void shouldThrowParseExceptionWhenTokenIsInvalid() throws ParseException, JOSEException {
//         // Arrange
//         String invalidToken = "invalid-token";
//         TokenRequest tokenRequest = new TokenRequest(invalidToken);

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);
//         doThrow(new ParseException("Invalid token format", 0))
//             .when(spyAuthenticationService).verifyToken(invalidToken, true);

//         // Act & Assert
//         assertThrows(ParseException.class, () -> spyAuthenticationService.refreshToken(tokenRequest));

//         verify(spyAuthenticationService).verifyToken(invalidToken, true);
//         verifyNoInteractions(userRepository);
//         verifyNoInteractions(invalidateTokenRepository);
//         verify(spyAuthenticationService, never()).generateToken(any(User.class), anyBoolean());
//     }

//     @Test
//     void shouldThrowJOSEExceptionWhenTokenSignatureIsInvalid() throws ParseException, JOSEException {
//         // Arrange
//         String badSignatureToken = "bad-signature-token";
//         TokenRequest tokenRequest = new TokenRequest(badSignatureToken);

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);
//         doThrow(new JOSEException("Invalid signature"))
//             .when(spyAuthenticationService).verifyToken(badSignatureToken, true);

//         // Act & Assert
//         assertThrows(JOSEException.class, () -> spyAuthenticationService.refreshToken(tokenRequest));

//         verify(spyAuthenticationService).verifyToken(badSignatureToken, true);
//         verifyNoInteractions(userRepository);
//         verifyNoInteractions(invalidateTokenRepository);
//         verify(spyAuthenticationService, never()).generateToken(any(User.class), anyBoolean());
//     }

//     @Test
//     void shouldRegisterUserSuccessfully() {
//         // Arrange
//         UserCreateRequest request = new UserCreateRequest();
//         request.setEmail("test@example.com");
//         request.setFullName("Test User");

//         UserResponse createdUserResponse = new UserResponse();
//         createdUserResponse.setEmail("test@example.com");

//         User mockUser = new User();
//         mockUser.setEmail("test@example.com");

//         String mockToken = "mock-verification-token";

//         when(userService.createUser(request)).thenReturn(createdUserResponse);
//         when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);
//         doReturn(mockToken).when(spyAuthenticationService).generateToken(mockUser, true);
//         doNothing().when(mailService).sendEmailVerify(request.getFullName(), mockToken, request.getEmail());

//         // Act
//         UserResponse response = spyAuthenticationService.registerUser(request);

//         // Assert
//         assertNotNull(response);
//         assertEquals("test@example.com", response.getEmail());

//         verify(userService).createUser(request);
//         verify(userRepository).findByEmail("test@example.com");
//         verify(spyAuthenticationService).generateToken(mockUser, true);
//         verify(mailService).sendEmailVerify(request.getFullName(), mockToken, request.getEmail());
//     }

//     @Test
//     void Registers_shouldThrowExceptionWhenUserCreationFails() {
//         // Arrange
//         UserCreateRequest request = new UserCreateRequest();
//         request.setEmail("test@example.com");

//         when(userService.createUser(request)).thenThrow(new AppException(ErrorCode.USER_EXISTED));

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.registerUser(request));
//         assertEquals(ErrorCode.USER_EXISTED, exception.getErrorCode());

//         verify(userService).createUser(request);
//         verifyNoInteractions(userRepository);
//         verifyNoInteractions(mailService);
//     }

//     @Test
//     void Registers_shouldThrowExceptionWhenUserNotFound() {
//         // Arrange
//         UserCreateRequest request = new UserCreateRequest();
//         request.setEmail("test@example.com");

//         UserResponse createdUserResponse = new UserResponse();
//         createdUserResponse.setEmail("test@example.com");

//         when(userService.createUser(request)).thenReturn(createdUserResponse);
//         when(userRepository.findByEmail("test@example.com")).thenReturn(null);

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.registerUser(request));
//         assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION, exception.getErrorCode());

//         verify(userService).createUser(request);
//         verify(userRepository).findByEmail("test@example.com");
//         verifyNoInteractions(mailService);
//     }

//     @Test
//     void Registers_shouldThrowExceptionWhenMailServiceFails() {
//         // Arrange
//         UserCreateRequest request = new UserCreateRequest();
//         request.setEmail("test@example.com");
//         request.setFullName("Test User");

//         UserResponse createdUserResponse = new UserResponse();
//         createdUserResponse.setEmail("test@example.com");

//         User mockUser = new User();
//         mockUser.setEmail("test@example.com");

//         String mockToken = "mock-verification-token";

//         when(userService.createUser(request)).thenReturn(createdUserResponse);
//         when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);
//         doReturn(mockToken).when(spyAuthenticationService).generateToken(mockUser, true);
//         doThrow(new RuntimeException("Mail sending failed")).when(mailService)
//             .sendEmailVerify(request.getFullName(), mockToken, request.getEmail());

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> spyAuthenticationService.registerUser(request));
//         assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION, exception.getErrorCode());

//         verify(userService).createUser(request);
//         verify(userRepository).findByEmail("test@example.com");
//         verify(spyAuthenticationService).generateToken(mockUser, true);
//         verify(mailService).sendEmailVerify(request.getFullName(), mockToken, request.getEmail());
//     }

//     @Test
//     void verifyEmail_Success() throws JOSEException, ParseException {
//         // Arrange
//         String mockToken = "valid-token";
//         String email = "test@example.com";

//         IntrospectResponse validResponse = IntrospectResponse.builder().valid(true).build();

//         SignedJWT signedJWT = mock(SignedJWT.class);
//         JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject(email).build();

//         User mockUser = new User();
//         mockUser.setEmail(email);
//         mockUser.setIsVerified(false);

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);

//         doReturn(validResponse).when(spyAuthenticationService).introspectToken(any(TokenRequest.class));
//         doReturn(signedJWT).when(spyAuthenticationService).verifyToken(mockToken, false);
//         doReturn(claimsSet).when(signedJWT).getJWTClaimsSet();

//         when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
//         when(userRepository.save(any(User.class))).thenReturn(mockUser);

//         // Act
//         boolean result = spyAuthenticationService.verifyEmail(mockToken);

//         // Assert
//         assertTrue(result);
//         assertTrue(mockUser.getIsVerified());

//         verify(spyAuthenticationService).introspectToken(any(TokenRequest.class));
//         verify(spyAuthenticationService).verifyToken(mockToken, false);
//         verify(userRepository).findByEmail(email);
//         verify(userRepository).save(mockUser);
//     }



//     @Test
//     void verifyEmail_InvalidToken_ShouldThrowException() throws JOSEException, ParseException {
//         // Arrange
//         String mockToken = "invalid-token";
//         TokenRequest tokenRequest = new TokenRequest(mockToken);
//         IntrospectResponse invalidResponse = IntrospectResponse.builder().valid(false).build();

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);

//         doReturn(invalidResponse).when(spyAuthenticationService).introspectToken(any(TokenRequest.class));

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> spyAuthenticationService.verifyEmail(mockToken));
//         assertEquals(ErrorCode.UNAUTHENTICATED, exception.getErrorCode());

//         verify(spyAuthenticationService).introspectToken(any(TokenRequest.class));
//         verify(spyAuthenticationService, never()).verifyToken(anyString(), anyBoolean());
//         verifyNoInteractions(userRepository);
//     }


//     @Test
//     void verifyEmail_UserNotFound_ShouldThrowException() throws JOSEException, ParseException {
//         // Arrange
//         String mockToken = "valid-token";
//         String email = "nonexistent@example.com";

//         TokenRequest tokenRequest = new TokenRequest(mockToken);
//         IntrospectResponse validResponse = IntrospectResponse.builder().valid(true).build();

//         SignedJWT signedJWT = mock(SignedJWT.class);
//         JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject(email).build();

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);

//         doReturn(validResponse).when(spyAuthenticationService).introspectToken(any(TokenRequest.class));
//         doReturn(signedJWT).when(spyAuthenticationService).verifyToken(mockToken, false);
//         doReturn(claimsSet).when(signedJWT).getJWTClaimsSet();
        
//         when(userRepository.findByEmail(email)).thenReturn(null);

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> spyAuthenticationService.verifyEmail(mockToken));
//         assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());

//         verify(spyAuthenticationService).introspectToken(any(TokenRequest.class));
//         verify(spyAuthenticationService).verifyToken(mockToken, false);
//         verify(userRepository).findByEmail(email);
//         verify(userRepository, never()).save(any(User.class));
//     }

//     @Test
//     void changePassword_Success() {
//         // Arrange
//         String email = "test@example.com";
//         ChangePasswordRequest cpRequest = new ChangePasswordRequest("oldPass", "newPass", "newPass");
//         User mockUser = new User();
//         mockUser.setEmail(email);
//         mockUser.setPassword("hashedOldPass");

//         when(authentication.getName()).thenReturn(email);
//         when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
//         when(passwordEncoder.matches(cpRequest.getOldPassword(), mockUser.getPassword())).thenReturn(true);
//         when(passwordEncoder.matches(cpRequest.getNewPassword(), mockUser.getPassword())).thenReturn(false);
//         when(passwordEncoder.encode(cpRequest.getNewPassword())).thenReturn("hashedNewPass");

//         // Act
//         boolean result = authenticationService.changePassword(cpRequest);

//         // Assert
//         assertTrue(result);
//         verify(userRepository).save(mockUser);
//         assertEquals("hashedNewPass", mockUser.getPassword());
//     }

//     @Test
//     void changePassword_UserNotFound_ShouldThrowException() {
//         // Arrange
//         String email = "notfound@example.com";
//         ChangePasswordRequest cpRequest = new ChangePasswordRequest("oldPass", "newPass", "newPass");

//         when(authentication.getName()).thenReturn(email);
//         when(userRepository.findByEmail(email)).thenReturn(null);

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.changePassword(cpRequest));
//         assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());

//         verify(userRepository).findByEmail(email);
//         verifyNoInteractions(passwordEncoder);
//     }

//     @Test
//     void changePassword_PasswordNotMatch_ShouldThrowException() {
//         // Arrange
//         String email = "test@example.com";
//         ChangePasswordRequest cpRequest = new ChangePasswordRequest("oldPass", "newPass", "wrongConfirm");
        
//         User mockUser = new User();
//         mockUser.setEmail(email);
//         mockUser.setPassword("encodedOldPass");

//         when(authentication.getName()).thenReturn(email);
//         when(securityContext.getAuthentication()).thenReturn(authentication);
//         SecurityContextHolder.setContext(securityContext);

//         when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.changePassword(cpRequest));
//         assertEquals(ErrorCode.PASSWORD_NOT_MATCH, exception.getErrorCode());

//         // Verify interactions
//         verify(userRepository).findByEmail(email);
//         verifyNoInteractions(passwordEncoder);
//     }


//     @Test
//     void changePassword_WrongOldPassword_ShouldThrowException() {
//         // Arrange
//         String email = "test@example.com";
//         ChangePasswordRequest cpRequest = new ChangePasswordRequest("wrongOldPass", "newPass", "newPass");
//         User mockUser = new User();
//         mockUser.setEmail(email);
//         mockUser.setPassword("hashedOldPass");

//         when(authentication.getName()).thenReturn(email);
//         when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
//         when(passwordEncoder.matches(cpRequest.getOldPassword(), mockUser.getPassword())).thenReturn(false);

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.changePassword(cpRequest));
//         assertEquals(ErrorCode.UNAUTHENTICATED, exception.getErrorCode());

//         verify(userRepository).findByEmail(email);
//         verify(passwordEncoder).matches(cpRequest.getOldPassword(), mockUser.getPassword());
//     }

//     @Test
//     void changePassword_DuplicatedPassword_ShouldThrowException() {
//         // Arrange
//         String email = "test@example.com";
//         ChangePasswordRequest cpRequest = new ChangePasswordRequest("oldPass", "newPass", "newPass");
//         User mockUser = new User();
//         mockUser.setEmail(email);
//         mockUser.setPassword("hashedOldPass");

//         when(authentication.getName()).thenReturn(email);
//         when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
//         when(passwordEncoder.matches(cpRequest.getOldPassword(), mockUser.getPassword())).thenReturn(true);
//         when(passwordEncoder.matches(cpRequest.getNewPassword(), mockUser.getPassword())).thenReturn(true);

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.changePassword(cpRequest));
//         assertEquals(ErrorCode.PASSWORD_DUPLICATED, exception.getErrorCode());

//         verify(userRepository).findByEmail(email);
//         verify(passwordEncoder).matches(cpRequest.getOldPassword(), mockUser.getPassword());
//         verify(passwordEncoder).matches(cpRequest.getNewPassword(), mockUser.getPassword());
//     }

//     @Test
//     void forgetPassword_UserNotFound_ShouldThrowException() {
//         // Arrange
//         String email = "test@example.com";

//         when(userRepository.findByEmail(email)).thenReturn(null);

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.forgetPassword(email));
//         assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());

//         // Verify interactions
//         verify(userRepository).findByEmail(email);
//         verifyNoInteractions(mailService);
//     }

//     @Test
//     void forgetPassword_EmailNotVerified_ShouldThrowException() {
//         // Arrange
//         String email = "test@example.com";
//         User mockUser = new User();
//         mockUser.setEmail(email);
//         mockUser.setIsVerified(false);

//         when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.forgetPassword(email));
//         assertEquals(ErrorCode.EMAIL_NOT_VERIFIED, exception.getErrorCode());

//         // Verify interactions
//         verify(userRepository).findByEmail(email);
//         verifyNoInteractions(mailService);
//     }

//     @Test
//     void forgetPassword_Success_ShouldSendOTP() {
//         // Arrange
//         String email = "test@example.com";
//         Integer mockOtp = 123456;
//         User mockUser = new User();
//         mockUser.setEmail(email);
//         mockUser.setIsVerified(true);
//         mockUser.setFullName("Test User");

//         when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        
//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);
//         when(spyAuthenticationService.generateOTP(email)).thenReturn(mockOtp);

//         // Act
//         spyAuthenticationService.forgetPassword(email);

//         // Assert
//         verify(userRepository).findByEmail(email);
//         verify(spyAuthenticationService).generateOTP(email);
//         verify(mailService).sendEmailOTP(mockOtp, mockUser.getEmail(), true, mockUser.getFullName());
//     }

//     @Test
//     void verifyOTP_OTPNotFound_ShouldThrowException() {
//         // Arrange
//         Integer otpToken = 123456;
//         String email = "test@example.com";

//         when(otpRepository.findByOtp(otpToken)).thenReturn(null);

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.verifyOTP(otpToken, email));
//         assertEquals(ErrorCode.OTP_NOT_EXISTED, exception.getErrorCode());

//         verify(otpRepository).findByOtp(otpToken);
//     }

//     @Test
//     void verifyOTP_OTPExpired_ShouldThrowException() {
//         // Arrange
//         Integer otpToken = 123456;
//         String email = "test@example.com";
//         OtpVerification expiredOtp = new OtpVerification();
//         expiredOtp.setOtp(otpToken);
//         expiredOtp.setExpiredAt(LocalDateTime.now().minusMinutes(1));

//         when(otpRepository.findByOtp(otpToken)).thenReturn(Optional.of(expiredOtp));

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.verifyOTP(otpToken, email));
//         assertEquals(ErrorCode.OTP_EXPIRED, exception.getErrorCode());

//         // Verify interactions
//         verify(otpRepository).findByOtp(otpToken);
//     }

//     @Test
//     void verifyOTP_Success_ShouldReturnToken() {
//         // Arrange
//         Integer otpToken = 123456;
//         String mockJwtToken = "mock-jwt-token";

//         OtpVerification validOtp = new OtpVerification();
//         validOtp.setOtp(otpToken);
//         validOtp.setExpiredAt(LocalDateTime.now().plusMinutes(10)); // OTP còn hạn

        
//         ReflectionTestUtils.setField(authenticationService, "SIGN_KEY", "testSignKey");
//         ReflectionTestUtils.setField(authenticationService, "VALID_DURATION", 3600L);
//         ReflectionTestUtils.setField(authenticationService, "REFRESH_DURATION", 7200L);
//         ReflectionTestUtils.setField(authenticationService, "MAIL_DURATION", 1800L);

        
//         when(otpRepository.findByOtp(otpToken)).thenReturn(Optional.of(validOtp));
//         when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        
//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);
//         doReturn(mockJwtToken).when(spyAuthenticationService).generateToken(any(User.class), anyBoolean());

//         // Act
//         AuthenticationResponse response = spyAuthenticationService.verifyOTP(otpToken, user.getEmail());

//         // Assert
//         assertNotNull(response);
//         assertTrue(response.isAuthenticate());
//         assertEquals(mockJwtToken, response.getToken());

//         // Verify interactions
//         verify(otpRepository).findByOtp(otpToken);
//         verify(userRepository).findByEmail(user.getEmail());
//         verify(spyAuthenticationService).generateToken(user, false);
//     }

//     @Test
//     void resetPassword_InvalidToken_ShouldThrowException() throws JOSEException, ParseException {
//         // Arrange
//         String mockToken = "invalid-token";

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);
//         doThrow(new AppException(ErrorCode.JWT_TOKEN_INVALID))
//             .when(spyAuthenticationService).verifyToken(mockToken, false);

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> spyAuthenticationService.resetPassword(mockToken));
//         assertEquals(ErrorCode.JWT_TOKEN_INVALID, exception.getErrorCode());

//         verify(spyAuthenticationService).verifyToken(mockToken, false);
//         verifyNoInteractions(userRepository);
//     }

//     @Test
//     void resetPassword_Success_ShouldReturnNewPassword() throws JOSEException, ParseException {
//         // Arrange
//         String token = "valid.jwt.token";
//         String email = "user@example.com";
//         User mockUser = new User();
//         mockUser.setEmail(email);

//         ReflectionTestUtils.setField(authenticationService, "SIGN_KEY", "testSignKey");
//         ReflectionTestUtils.setField(authenticationService, "VALID_DURATION", 3600L);
//         ReflectionTestUtils.setField(authenticationService, "REFRESH_DURATION", 7200L);
//         ReflectionTestUtils.setField(authenticationService, "MAIL_DURATION", 1800L);

//         SignedJWT signedJWT = mock(SignedJWT.class);
//         JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject(email).build();

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);
//         doReturn(signedJWT).when(spyAuthenticationService).verifyToken(token, false);
//         doReturn(claimsSet).when(signedJWT).getJWTClaimsSet();
//         when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
//         when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

//         // Act
//         String newPassword = spyAuthenticationService.resetPassword(token);

//         // Assert
//         assertNotNull(newPassword);
//         verify(userRepository).save(mockUser);
//         verify(spyAuthenticationService).logout(any(TokenRequest.class));
//     }

//     @Test
//     void resetPassword_UserNotFound_ShouldThrowUserNotExisted() throws JOSEException, ParseException {
//         // Arrange
//         String token = "valid.jwt.token";
//         String email = "user@example.com";

//         SignedJWT signedJWT = mock(SignedJWT.class);
//         JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject(email).build();

//         AuthenticationServiceImpl spyAuthenticationService = spy(authenticationService);
//         doReturn(signedJWT).when(spyAuthenticationService).verifyToken(token, false);
//         doReturn(claimsSet).when(signedJWT).getJWTClaimsSet();
//         when(userRepository.findByEmail(email)).thenReturn(null);

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> spyAuthenticationService.resetPassword(token));
//         assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
//     }

//     @Test
//     void verifyChangeEmail_Success_ShouldUpdateEmail() {
//         // Arrange
//         VerifyChangeMailRequest request = VerifyChangeMailRequest.builder()
//                                                                 .newEmail("new@example.com")
//                                                                 .oldEmail("old@example.com")
//                                                                 .otp(123123)
//                                                                 .build();
        
//         OtpVerification mockOtp = new OtpVerification();
//         mockOtp.setOtp(123456);
//         mockOtp.setExpiredAt(LocalDateTime.now().plusMinutes(5)); // OTP còn hạn

//         User mockUser = new User();
//         mockUser.setEmail("old@example.com");

//         when(otpRepository.findByOtp(anyInt())).thenReturn(Optional.of(mockOtp)); 
//         when(userRepository.findByEmail("old@example.com")).thenReturn(Optional.of(mockUser));

//         // Act
//         authenticationService.verifyChangeEmail(request);

//         // Assert
//         assertEquals("new@example.com", mockUser.getEmail());
//         verify(userRepository).save(mockUser);
//     }

//     @Test
//     void verifyChangeEmail_OTPNotExisted_ShouldThrowException() {
//         // Arrange
//         VerifyChangeMailRequest request = VerifyChangeMailRequest.builder()
//         .newEmail("new@example.com")
//         .oldEmail("old@example.com")
//         .otp(123123)
//         .build();

//         when(otpRepository.findByOtp(123456)).thenReturn(null); // Không có OTP

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.verifyChangeEmail(request));
//         assertEquals(ErrorCode.OTP_NOT_EXISTED, exception.getErrorCode());
//         verifyNoInteractions(userRepository);
//     }

//     @Test
//     void verifyChangeEmail_UserNotExisted_ShouldThrowException() {
//         // Arrange
//         VerifyChangeMailRequest request = VerifyChangeMailRequest.builder()
//                                                                 .newEmail("new@example.com")
//                                                                 .oldEmail("old@example.com")
//                                                                 .otp(123123)
//                                                                 .build();
        
//         OtpVerification mockOtp = new OtpVerification();
//         mockOtp.setOtp(123123);
//         mockOtp.setExpiredAt(LocalDateTime.now().plusMinutes(5));

       
//         when(otpRepository.findByOtp(anyInt())).thenReturn(Optional.of(mockOtp)); 
//         when(userRepository.findByEmail("old@example.com")).thenReturn(null); 

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.verifyChangeEmail(request));
//         assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
//     }

//     @Test
//     void verifyChangeEmail_OTPExpired_ShouldThrowException() {
//         // Arrange
//         VerifyChangeMailRequest request = VerifyChangeMailRequest.builder()
//                                                                 .newEmail("new@example.com")
//                                                                 .oldEmail("old@example.com")
//                                                                 .otp(123123)
//                                                                 .build();
        
//         OtpVerification mockOtp = new OtpVerification();
//         mockOtp.setOtp(123123);
//         mockOtp.setExpiredAt(LocalDateTime.now().minusMinutes(1));

//         User mockUser = new User();
//         mockUser.setEmail("old@example.com");


//         when(otpRepository.findByOtp(123123)).thenReturn(Optional.of(mockOtp));
//         when(userRepository.findByEmail("old@example.com")).thenReturn(Optional.of(mockUser));

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.verifyChangeEmail(request));
//         assertEquals(ErrorCode.OTP_EXPIRED, exception.getErrorCode());
//     }


//     @Test
//     void changeEmail_Fail_ShouldThrowUnauthenticated() {
//         // Arrange
//         ChangeMailRequest request = new ChangeMailRequest("old@example.com", "new@example.com");

//         when(securityContext.getAuthentication()).thenReturn(null);

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.changeEmail(request));
//         assertEquals(ErrorCode.UNAUTHENTICATED, exception.getErrorCode());
//     }

//     @Test
//     void changeEmail_UnauthenticatedUser_ShouldThrowException() {
//         // Arrange
//         ChangeMailRequest request = new ChangeMailRequest("wrong@example.com", "new@example.com");

//         Authentication authentication = mock(Authentication.class);
//         SecurityContext securityContext = mock(SecurityContext.class);
        
//         when(authentication.getName()).thenReturn("old@example.com");
//         when(securityContext.getAuthentication()).thenReturn(authentication);
//         SecurityContextHolder.setContext(securityContext);

//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.changeEmail(request));
//         assertEquals(ErrorCode.UNAUTHENTICATED, exception.getErrorCode());
//     }

//     @Test
//     void changeEmail_Fail_ShouldThrowUserNotExisted() {
//         // Arrange
//         ChangeMailRequest request = new ChangeMailRequest("old@example.com", "new@example.com");
//         Authentication authentication = mock(Authentication.class);
//         SecurityContext securityContext = mock(SecurityContext.class);
//         when(securityContext.getAuthentication()).thenReturn(authentication);
//         SecurityContextHolder.setContext(securityContext);

//         when(authentication.getName()).thenReturn("old@example.com");
//         when(authentication.isAuthenticated()).thenReturn(true);
//         when(userRepository.findByEmail("old@example.com")).thenReturn(null);

//         AppException exception = assertThrows(AppException.class, () -> authenticationService.changeEmail(request));
//         assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
//     }

//     @Test
//     void changeEmail_UserNotFound_ShouldThrowException() {
//         // Arrange
//         ChangeMailRequest request = new ChangeMailRequest("testuser@example.com", "new@example.com");
//         Authentication authentication = mock(Authentication.class);
//         SecurityContext securityContext = mock(SecurityContext.class);
        
//         when(securityContext.getAuthentication()).thenReturn(authentication);
//         SecurityContextHolder.setContext(securityContext);

//         when(authentication.getName()).thenReturn(request.getOldEmail());  
//         when(authentication.isAuthenticated()).thenReturn(true);

//         User mockUser = new User();
//         mockUser.setId(1L);
//         mockUser.setEmail(request.getOldEmail());
//         mockUser.setFullName("Test User");
//         when(userRepository.findByEmail("old@example.com")).thenReturn(null);
//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.changeEmail(request));
//         assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
//     }

//     @Test
//     void changeEmail_Success() {
//         // Arrange
//         ChangeMailRequest request = new ChangeMailRequest("testuser@example.com", "new@example.com");
//         Authentication authentication = mock(Authentication.class);
//         SecurityContext securityContext = mock(SecurityContext.class);
        
//         when(securityContext.getAuthentication()).thenReturn(authentication);
//         SecurityContextHolder.setContext(securityContext);

//         when(authentication.getName()).thenReturn(request.getOldEmail());  
//         when(authentication.isAuthenticated()).thenReturn(true);

//         User mockUser = new User();
//         mockUser.setId(1L);
//         mockUser.setEmail(request.getOldEmail());
//         mockUser.setFullName("Test User");

//         when(userRepository.findByEmail(request.getOldEmail())).thenReturn(Optional.of(mockUser));

//         doNothing().when(mailService).sendEmailOTP(anyInt(), eq(request.getNewEmail()), eq(false), eq("Test User"));
//         doNothing().when(mailService).sendSimpleEmail(eq(request.getOldEmail()), anyString(), anyString());

//         assertDoesNotThrow(() -> authenticationService.changeEmail(request));

//         verify(mailService).sendEmailOTP(anyInt(), eq(request.getNewEmail()), eq(false), eq("Test User"));
//         verify(mailService).sendSimpleEmail(eq(request.getOldEmail()), anyString(), anyString());
//     }

//     @Test
//     void buildScope_UserIsNull_ShouldThrowException() {
//         // Act & Assert
//         AppException exception = assertThrows(AppException.class, () -> authenticationService.buildScope(null));
//         assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
//     }

//     @Test
//     void buildScope_UserHasNoRoles_ShouldReturnEmptyString() {
//         // Arrange
//         User user = new User();
//         user.setRoles(Collections.emptySet());

//         // Act
//         String result = authenticationService.buildScope(user);

//         // Assert
//         assertEquals("", result);
//     }

//     @Test
//     void buildScope_UserHasOneRole_ShouldReturnCorrectRole() {
//         // Act
//         String result = authenticationService.buildScope(user);

//         // Assert
//         assertEquals("ROLE_USER", result);
//     }

//     @Test
//     void buildScope_UserHasMultipleRoles_ShouldReturnAllRoles() {
//         // Arrange
//         Role role1 = new Role();
//         role1.setName("ADMIN");
//         Role role2 = new Role();
//         role2.setName("USER");

//         // Sử dụng Set thay vì List
//         Set<Role> roles = new HashSet<>();
//         roles.add(role1);
//         roles.add(role2);
//         user.setRoles(roles);

//         // Act
//         String result = authenticationService.buildScope(user);

//         // Assert
//         assertTrue(result.contains("ROLE_ADMIN"));
//         assertTrue(result.contains("ROLE_USER"));
//         assertEquals("ROLE_USER ROLE_ADMIN", result);
//     }
// }
