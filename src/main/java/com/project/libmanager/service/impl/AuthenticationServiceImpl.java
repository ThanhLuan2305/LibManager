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
import com.project.libmanager.service.*;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements IAuthenticationService {
    private final UserRepository userRepository;
    private final IMaintenanceService maintenanceService;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final IUserService userService;
    private final ILoginDetailService loginDetailService;
    private final CommonUtil commonUtil;
    private final IActivityLogService activityLogService;
    private final CookieUtil cookieUtil;

    @Value("${jwt.refresh-duration}")
    private long refreshDuration;

    @Value("${jwt.valid-duration}")
    private long validDuration;

    private static final String ACCESS_TOKEN_STR ="accessToken";
    private static final String REFRESH_TOKEN_STR ="refreshToken";

    /**
     * Authenticates a user based on email and password.
     *
     * @param aRequest the authentication request containing email and password.
     * @return an {@link AuthenticationResponse} containing authentication status
     * and token.
     * @throws AppException if the user does not exist, email is not verified, or
     *                      password is incorrect.
     * @implNote This method verifies user credentials and generates a JWT token
     * upon successful authentication.
     */
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest aRequest, HttpServletResponse response) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(aRequest.getEmail(), aRequest.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User userDB = userService.findByEmail(aRequest.getEmail());

        Role role = roleRepository.findByName(PredefinedRole.USER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        if (maintenanceService.isMaintenanceMode() && userDB.getRoles().contains(role)) {
            throw new AppException(ErrorCode.MAINTENACE_MODE);
        }
        String jti = commonUtil.generateJTI();
        String accessToken = jwtTokenProvider.generateToken(userDB, TokenType.ACCESS, jti);
        String refreshToken = jwtTokenProvider.generateToken(userDB, TokenType.REFRESH, jti);

        saveRefreshToken(refreshToken);


        cookieUtil.removeCookie(response, ACCESS_TOKEN_STR);
        cookieUtil.removeCookie(response, REFRESH_TOKEN_STR);
        cookieUtil.addCookie(response, ACCESS_TOKEN_STR, accessToken,(int)validDuration);
        cookieUtil.addCookie(response, REFRESH_TOKEN_STR, refreshToken, (int)refreshDuration);

        activityLogService.logAction(
                userDB.getId(),
                userDB.getEmail(),
                UserAction.LOGIN,
                "User login success!!!",
                null,
                null
        );

        return AuthenticationResponse.builder().accessToken(accessToken).refreshToken(refreshToken)
                .build();
    }

    private void saveRefreshToken(String token) {
        try {
            SignedJWT signedJWT = jwtTokenProvider.verifyToken(token, true);
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            loginDetailService.createLoginDetail(LoginDetailRequest
                    .builder()
                    .email(claimsSet.getSubject())
                    .jti(claimsSet
                            .getJWTID())
                    .enabled(true)
                    .expiredAt(claimsSet
                            .getExpirationTime()
                            .toInstant())
                    .build());
        } catch (Exception e) {
            log.error("Error saving refresh token: {}", e.getMessage());
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    /**
     * Logs out a user by invalidating their access and refresh tokens.
     *
     * @param accessToken The request containing the access and refresh tokens to be invalidated.
     * @throws AppException If the tokens are already expired or if there is a failure during logout.
     */
    @Override
    public void logout(String accessToken, HttpServletResponse response) {
        try {
            SignedJWT signedJWT = jwtTokenProvider.verifyToken(accessToken, false);
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            String jwtID = claimsSet.getJWTID();
            loginDetailService.disableLoginDetailById(jwtID);
            cookieUtil.removeCookie(response, ACCESS_TOKEN_STR);
            cookieUtil.removeCookie(response, REFRESH_TOKEN_STR);

            User userDB = userService.findByEmail(claimsSet.getSubject());
            activityLogService.logAction(
                    userDB.getId(),
                    userDB.getEmail(),
                    UserAction.LOGOUT,
                    "User log out success!!!",
                    null,
                    null
            );
        } catch (Exception e) {
            log.error("Error logout token", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    /**
     * Refreshes the authentication token by verifying the provided refresh token,
     * invalidating the old token,
     * and generating a new access token and refresh token.
     *
     * @param refreshToken the request containing the refresh token.
     * @return an AuthenticationResponse containing the new access token and refresh
     * token.
     * @throws AppException if the user associated with the token does not exist.
     * @implNote This method verifies the refresh token, invalidates the old token
     * by storing it in the database,
     * retrieves the user associated with the token, and generates new
     * authentication tokens.
     */
    @Override
    public AuthenticationResponse refreshToken(String refreshToken, HttpServletResponse response) {
        JWTClaimsSet claimsSet;
        String typeToken;
        SignedJWT signedJWT = jwtTokenProvider.verifyToken(refreshToken, true);

        try {
            claimsSet = signedJWT.getJWTClaimsSet();
            typeToken = claimsSet.getStringClaim("type");
        } catch (ParseException e) {
            log.error("Error parsing token claims", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (!TokenType.REFRESH.name().equals(typeToken)) {
            log.error("Token is not refresh token");
            throw new AppException(ErrorCode.JWT_TOKEN_INVALID);
        }

        String jwtID = claimsSet.getJWTID();
        String email = claimsSet.getSubject();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String accessTokenGrt= jwtTokenProvider.generateToken(user, TokenType.ACCESS, jwtID);
        String refreshTokenGrt = jwtTokenProvider.renewRefreshToken(user, jwtID);

        loginDetailService.updateLoginDetailIsEnable(jwtID, Instant.now().plus(refreshDuration, ChronoUnit.SECONDS));

        cookieUtil.removeCookie(response, ACCESS_TOKEN_STR);
        cookieUtil.removeCookie(response, REFRESH_TOKEN_STR);
        cookieUtil.addCookie(response, ACCESS_TOKEN_STR, accessTokenGrt,(int) validDuration);
        cookieUtil.addCookie(response, REFRESH_TOKEN_STR, refreshTokenGrt, (int)refreshDuration);
        return AuthenticationResponse.builder()
                .accessToken(accessTokenGrt)
                .refreshToken(refreshTokenGrt)
                .build();
    }
}
