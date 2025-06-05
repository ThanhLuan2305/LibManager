package com.project.libmanager.service.impl;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.constant.TokenType;
import com.project.libmanager.constant.UserAction;
import com.project.libmanager.constant.VerificationStatus;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.repository.UserRepository;
import com.project.libmanager.security.JwtTokenProvider;
import com.project.libmanager.service.IActivityLogService;
import com.project.libmanager.service.IMailService;
import com.project.libmanager.service.IPasswordService;
import com.project.libmanager.service.dto.request.ChangePasswordRequest;
import com.project.libmanager.service.dto.request.ResetPasswordRequest;
import com.project.libmanager.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;

/**
 * Implementation of {@link IPasswordService} for managing password-related operations.
 * Handles password changes, reset requests, and token-based password resets with audit logging.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordServiceImpl implements IPasswordService {
    private final UserRepository userRepository;        // Handles user data access and persistence
    private final IMailService mailService;             // Sends emails for password reset
    private final CommonUtil commonUtil;                // Provides utility functions like JTI generation
    private final PasswordEncoder passwordEncoder;      // Encrypts passwords using configured algorithm
    private final JwtTokenProvider jwtTokenProvider;    // Manages JWT token generation and verification
    private final IActivityLogService activityLogService; // Logs user actions for audit trail

    /**
     * Changes the user's password after verifying the old password.
     *
     * @param cpRequest the {@link ChangePasswordRequest} containing:
     *                  - oldPassword: current password to verify (required)
     *                  - newPassword: new password to set (required)
     *                  - confirmPassword: confirmation of new password (assumed validated upstream)
     * @return true if the password change is successful
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - user not found (ErrorCode.USER_NOT_EXISTED)
     *                      - old password incorrect (ErrorCode.UNAUTHENTICATED)
     *                      - new password matches old password (ErrorCode.PASSWORD_DUPLICATED)
     * @implNote Verifies old password, ensures new password is different, encrypts it, and logs the action.
     */
    @Override
    public boolean changePassword(ChangePasswordRequest cpRequest) {
        // Fetch security context; assumes JWT-based authentication is configured
        SecurityContext jwtContex = SecurityContextHolder.getContext();
        // Extract email from authenticated principal; assumes email is the subject
        String email = jwtContex.getAuthentication().getName();

        // Retrieve user by email; fails fast if user doesn't exist
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Verify old password matches stored hash; uses PasswordEncoder for security
        boolean rs = passwordEncoder.matches(cpRequest.getOldPassword(), user.getPassword());
        // Fail if old password doesn't match; reuses UNAUTHENTICATED for simplicity
        if (!rs) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH); // Indicates authentication failure
        }

        // Check if new password is different from old; prevents redundant updates
        if (passwordEncoder.matches(cpRequest.getNewPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_DUPLICATED); // Enforces password change policy
        }

        // Encrypt new password and update user entity; assumes encoder is consistent
        user.setPassword(passwordEncoder.encode(cpRequest.getNewPassword()));
        // Persist updated user; assumes no concurrent modifications
        userRepository.save(user);
        // Log action for audit; no old/new state needed as password is sensitive
        activityLogService.logAction(
                user.getId(),
                user.getEmail(),
                UserAction.PASSWORD_CHANGED,
                "User changed new password successfully with email: " + user.getEmail(),
                null,
                null
        );
        // Return success; boolean indicates operation completed without errors
        return true;
    }

    /**
     * Initiates a password reset by generating a token and sending it via email.
     *
     * @param email the email address of the user requesting a password reset
     * @throws AppException if:
     *                      - user not found (ErrorCode.USER_NOT_EXISTED)
     *                      - user's email not fully verified (ErrorCode.USER_NOT_VERIFIED)
     * @implNote Generates a unique JTI and JWT token, logs the request, and sends reset email.
     */
    @Override
    public void forgetPassword(String email) {
        // Fetch user by email; fails fast if user doesn't exist
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        // Ensure user is fully verified; restricts reset to verified accounts
        if (!user.getVerificationStatus().equals(VerificationStatus.FULLY_VERIFIED)) {
            throw new AppException(ErrorCode.USER_NOT_VERIFIED); // Enforces verification policy
        }
        // Generate unique JTI (JWT ID) for token tracking
        String jti = commonUtil.generateJTI();
        // Create reset token with user details and JTI; assumes short expiration
        String token = jwtTokenProvider.generateToken(user, TokenType.RESET_PASSWORD, jti);

        // Log request for audit; no state change yet
        activityLogService.logAction(
                user.getId(),
                user.getEmail(),
                UserAction.PASSWORD_RESET_REQUEST,
                "User request reset passowrd with email: " + user.getEmail(),
                null,
                null
        );
        // Send reset email with token; assumes mail service handles delivery
        mailService.sendEmailResetPassword(user.getFullName(), email, token);
    }

    /**
     * Resets a user's password using a provided token and new password.
     *
     * @param resetPasswordRequest the {@link ResetPasswordRequest} containing:
     *                             - token: JWT reset token (required)
     *                             - newPassword: new password to set (required)
     * @throws AppException if:
     *                      - token is invalid or parsing fails (ErrorCode.UNAUTHENTICATED)
     *                      - token type is not RESET_PASSWORD (ErrorCode.JWT_TOKEN_INVALID)
     *                      - user not found (ErrorCode.USER_NOT_EXISTED)
     * @implNote Verifies token  Validates token, updates password, and logs the action.
     */
    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        // Declare variables for JWT claims and token type
        JWTClaimsSet claimsSet;
        String typeToken;
        // Verify token signature and validity; throws exception if invalid
        SignedJWT signedJWT = jwtTokenProvider.verifyToken(resetPasswordRequest.getToken());

        try {
            // Extract claims from token; contains subject (email) and type
            claimsSet = signedJWT.getJWTClaimsSet();
            // Get token type from claims; used to validate purpose
            typeToken = claimsSet.getStringClaim("type");
        } catch (ParseException e) {
            log.error("Error parsing token claims", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED); // Fail if claims can't be parsed
        }
        // Validate token type; ensures token is for password reset
        if (!TokenType.RESET_PASSWORD.name().equals(typeToken)) {
            log.error("Token is not refresh token");
            throw new AppException(ErrorCode.JWT_TOKEN_INVALID); // Fail if wrong token type
        }
        // Fetch user by email from token subject; assumes email is unique
        User user = userRepository.findByEmail(claimsSet.getSubject()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Encrypt new password and update user entity
        user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        // Persist updated user; assumes no concurrent updates
        userRepository.save(user);

        // Log successful reset; no state change tracked
        activityLogService.logAction(
                user.getId(),
                user.getEmail(),
                UserAction.PASSWORD_RESET_SUCCESS,
                "User reset passowrd success with email: " + user.getEmail(),
                null,
                null
        );
    }
}