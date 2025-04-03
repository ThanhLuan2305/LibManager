package com.project.libmanager.service.impl;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.constant.PredefinedRole;
import com.project.libmanager.constant.TokenType;
import com.project.libmanager.constant.UserAction;
import com.project.libmanager.entity.Role;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.repository.RoleRepository;
import com.project.libmanager.repository.UserRepository;
import com.project.libmanager.security.JwtTokenProvider;
import com.project.libmanager.service.IActivityLogService;
import com.project.libmanager.service.IAuthenticationService;
import com.project.libmanager.service.IMaintenanceService;
import com.project.libmanager.service.ILoginDetailService;
import com.project.libmanager.service.IUserService;
import com.project.libmanager.service.dto.request.AuthenticationRequest;
import com.project.libmanager.service.dto.request.LoginDetailRequest;
import com.project.libmanager.service.dto.response.AuthenticationResponse;
import com.project.libmanager.util.CommonUtil;
import com.project.libmanager.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Implementation of {@link IAuthenticationService} for handling user authentication operations.
 * Manages login, logout, and token refresh processes using JWT-based authentication.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements IAuthenticationService {
    private final UserRepository userRepository;              // Repository for user data access
    private final IMaintenanceService maintenanceService;     // Service to check maintenance mode
    private final RoleRepository roleRepository;              // Repository for role lookups
    private final JwtTokenProvider jwtTokenProvider;          // Utility for JWT token generation and verification
    private final AuthenticationManagerBuilder authenticationManagerBuilder; // Builds authentication manager
    private final IUserService userService;                   // Service for user-related operations
    private final ILoginDetailService loginDetailService;     // Service for managing login details
    private final CommonUtil commonUtil;                      // Utility for common functions (e.g., JTI generation)
    private final IActivityLogService activityLogService;     // Service for logging user actions
    private final CookieUtil cookieUtil;                      // Utility for cookie management

    @Value("${jwt.refresh-duration}")
    private long refreshDuration;                             // Duration (in seconds) for refresh token validity

    @Value("${jwt.valid-duration}")
    private long validDuration;                               // Duration (in seconds) for access token validity

    private static final String ACCESS_TOKEN_STR = "accessToken"; // Cookie key for access token
    private static final String REFRESH_TOKEN_STR = "refreshToken"; // Cookie key for refresh token

    /**
     * Authenticates a user with email and password, issuing JWT tokens upon success.
     *
     * @param aRequest the {@link AuthenticationRequest} containing:
     *                 - email: user's email (required)
     *                 - password: user's password (required)
     * @param response the {@link HttpServletResponse} to set authentication cookies
     * @return an {@link AuthenticationResponse} with access and refresh tokens
     * @throws AppException if:
     *                      - authentication fails (ErrorCode.UNAUTHENTICATED)
     *                      - user not found (ErrorCode.USER_NOT_EXISTED)
     *                      - system in maintenance mode for non-admins (ErrorCode.MAINTENACE_MODE)
     *                      - role not found (ErrorCode.ROLE_NOT_EXISTED)
     * @implNote Authenticates via Spring Security, generates tokens, stores refresh token details,
     * sets cookies, and logs the login action.
     */
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest aRequest, HttpServletResponse response) {
        // Create authentication token; email as principal, password as credential
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                aRequest.getEmail(), aRequest.getPassword());
        // Authenticate using Spring Security; throws exception if credentials invalid
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        // Set authentication in security context; enables downstream access
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Fetch user; assumes email uniqueness
        User userDB = userService.findByEmail(aRequest.getEmail());

        // Check maintenance mode; restricts non-admin users
        Role role = roleRepository.findByName(PredefinedRole.USER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        if (maintenanceService.isMaintenanceMode() && userDB.getRoles().contains(role)) {
            throw new AppException(ErrorCode.MAINTENACE_MODE); // Typo: MAINTENANCE_MODE
        }

        // Generate unique JTI and tokens; JTI links access and refresh tokens
        String jti = commonUtil.generateJTI();
        String accessToken = jwtTokenProvider.generateToken(userDB, TokenType.ACCESS, jti);
        String refreshToken = jwtTokenProvider.generateToken(userDB, TokenType.REFRESH, jti);

        // Save refresh token details; tracks session validity
        saveRefreshToken(refreshToken);

        // Clear existing cookies; ensures clean state
        cookieUtil.removeCookie(response, ACCESS_TOKEN_STR);
        cookieUtil.removeCookie(response, REFRESH_TOKEN_STR);
        // Set new cookies; casts durations to int (assumes seconds)
        cookieUtil.addCookie(response, ACCESS_TOKEN_STR, accessToken, (int) validDuration);
        cookieUtil.addCookie(response, REFRESH_TOKEN_STR, refreshToken, (int) refreshDuration);

        // Log successful login; audit trail for user action
        activityLogService.logAction(
                userDB.getId(),
                userDB.getEmail(),
                UserAction.LOGIN,
                "User login success!!!",
                null,
                null
        );

        // Return tokens in response DTO
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Saves refresh token details to track session validity.
     *
     * @param token the refresh token to save
     * @throws AppException if token verification or saving fails (ErrorCode.UNAUTHENTICATED)
     * @implNote Verifies token, extracts claims, and stores login details via LoginDetailService.
     */
    private void saveRefreshToken(String token) {
        try {
            // Verify token; ensures integrity and authenticity
            SignedJWT signedJWT = jwtTokenProvider.verifyToken(token);
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            // Save login details; includes email, JTI, and expiration
            loginDetailService.createLoginDetail(LoginDetailRequest
                    .builder()
                    .email(claimsSet.getSubject())
                    .jti(claimsSet.getJWTID())
                    .enabled(true) // Marks session as active
                    .expiredAt(claimsSet.getExpirationTime().toInstant())
                    .build());
        } catch (Exception e) {
            // Log error; provides context for debugging
            log.error("Error saving refresh token: {}", e.getMessage());
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    /**
     * Logs out a user by invalidating their tokens and clearing cookies.
     *
     * @param accessToken the access token to invalidate
     * @param response    the {@link HttpServletResponse} to clear authentication cookies
     * @throws AppException if token verification or logout fails (ErrorCode.UNAUTHENTICATED)
     * @implNote Disables login details by JTI, clears cookies, and logs the logout action.
     */
    @Override
    public void logout(String accessToken, HttpServletResponse response) {
        try {
            // Verify access token; ensures it's valid before proceeding
            SignedJWT signedJWT = jwtTokenProvider.verifyToken(accessToken);
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            // Disable session; uses JTI to invalidate both tokens
            String jwtID = claimsSet.getJWTID();
            loginDetailService.disableLoginDetailById(jwtID);

            // Clear cookies; removes tokens from client
            cookieUtil.removeCookie(response, ACCESS_TOKEN_STR);
            cookieUtil.removeCookie(response, REFRESH_TOKEN_STR);

            // Fetch user; assumes subject is email
            User userDB = userService.findByEmail(claimsSet.getSubject());
            // Log logout action; audit trail for user activity
            activityLogService.logAction(
                    userDB.getId(),
                    userDB.getEmail(),
                    UserAction.LOGOUT,
                    "User log out success!!!",
                    null,
                    null
            );
        } catch (Exception e) {
            // Log error with stack trace; aids debugging
            log.error("Error logout token", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    /**
     * Refreshes authentication tokens using a valid refresh token.
     *
     * @param refreshToken the refresh token to verify and refresh
     * @param response     the {@link HttpServletResponse} to update authentication cookies
     * @return an {@link AuthenticationResponse} with new access and refresh tokens
     * @throws AppException if:
     *                      - token is invalid or not a refresh token (ErrorCode.JWT_TOKEN_INVALID)
     *                      - user not found (ErrorCode.USER_NOT_EXISTED)
     *                      - parsing fails (ErrorCode.UNAUTHENTICATED)
     * @implNote Verifies refresh token, updates session expiration, generates new tokens,
     * and updates cookies.
     */
    @Override
    public AuthenticationResponse refreshToken(String refreshToken, HttpServletResponse response) {
        JWTClaimsSet claimsSet;
        String typeToken;
        // Verify token; ensures it's valid and unexpired
        SignedJWT signedJWT = jwtTokenProvider.verifyToken(refreshToken);

        try {
            // Extract claims; contains token metadata
            claimsSet = signedJWT.getJWTClaimsSet();
            typeToken = claimsSet.getStringClaim("type");
        } catch (ParseException e) {
            // Log parsing error; indicates malformed token
            log.error("Error parsing token claims", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Validate token type; ensures it's a refresh token
        if (!TokenType.REFRESH.name().equals(typeToken)) {
            log.error("Token is not refresh token");
            throw new AppException(ErrorCode.JWT_TOKEN_INVALID);
        }

        // Extract JTI and email; reuses JTI for continuity
        String jwtID = claimsSet.getJWTID();
        String email = claimsSet.getSubject();

        // Fetch user; fails if not found
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Generate new tokens; keeps same JTI
        String accessTokenGrt = jwtTokenProvider.generateToken(user, TokenType.ACCESS, jwtID);
        String refreshTokenGrt = jwtTokenProvider.renewRefreshToken(user, jwtID);

        // Update session expiration; extends refresh token validity
        loginDetailService.updateLoginDetailIsEnable(jwtID, Instant.now().plus(refreshDuration, ChronoUnit.SECONDS));

        // Update cookies; replaces old tokens
        cookieUtil.removeCookie(response, ACCESS_TOKEN_STR);
        cookieUtil.removeCookie(response, REFRESH_TOKEN_STR);
        cookieUtil.addCookie(response, ACCESS_TOKEN_STR, accessTokenGrt, (int) validDuration);
        cookieUtil.addCookie(response, REFRESH_TOKEN_STR, refreshTokenGrt, (int) refreshDuration);

        // Return new tokens in response DTO
        return AuthenticationResponse.builder()
                .accessToken(accessTokenGrt)
                .refreshToken(refreshTokenGrt)
                .build();
    }
}