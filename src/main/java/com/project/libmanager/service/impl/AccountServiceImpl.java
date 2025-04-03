package com.project.libmanager.service.impl;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.constant.OtpType;
import com.project.libmanager.constant.PredefinedRole;
import com.project.libmanager.constant.VerificationStatus;
import com.project.libmanager.constant.UserAction;
import com.project.libmanager.entity.OtpVerification;
import com.project.libmanager.entity.Role;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.repository.RoleRepository;
import com.project.libmanager.repository.UserRepository;
import com.project.libmanager.security.JwtTokenProvider;
import com.project.libmanager.service.IAccountService;
import com.project.libmanager.service.IActivityLogService;
import com.project.libmanager.service.IMailService;
import com.project.libmanager.service.ILoginDetailService;
import com.project.libmanager.service.IOtpVerificationService;
import com.project.libmanager.service.dto.request.ChangeMailRequest;
import com.project.libmanager.service.dto.request.RegisterRequest;
import com.project.libmanager.service.dto.request.VerifyChangeMailRequest;
import com.project.libmanager.service.dto.request.VerifyChangePhoneRequest;
import com.project.libmanager.service.dto.request.ChangePhoneRequest;
import com.project.libmanager.service.dto.response.UserResponse;
import com.project.libmanager.service.mapper.UserMapper;
import com.project.libmanager.util.CommonUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link IAccountService} for managing user account operations.
 * Handles user registration, email/phone verification, and role retrieval.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements IAccountService {
    private final UserRepository userRepository;           // Repository for user data persistence
    private final UserMapper userMapper;                   // Maps between User entity and DTOs
    private final IMailService mailService;                // Service for sending emails
    private final PasswordEncoder passwordEncoder;         // Encodes user passwords
    private final RoleRepository roleRepository;           // Repository for role lookups
    private final IOtpVerificationService otpVerificationService; // Manages OTP creation and verification
    private final IActivityLogService activityLogService;  // Logs user actions for auditing
    private final ILoginDetailService loginDetailService;  // Manages login session details
    private final JwtTokenProvider jwtTokenProvider;       // Handles JWT token generation and verification
    private final CommonUtil commonUtil;                   // Utility for common functions (e.g., OTP generation)

    /**
     * Registers a new user with provided details, assigns default role, and initiates verification.
     *
     * @param registerRequest the {@link RegisterRequest} containing:
     *                        - email: user's email (required)
     *                        - phoneNumber: user's phone number (required)
     *                        - password: user's password (required)
     *                        - fullName: user's full name (optional)
     * @return a {@link UserResponse} with the registered user's details
     * @throws AppException if:
     *                      - email already exists (ErrorCode.USER_EXISTED)
     *                      - phone number already exists (ErrorCode.PHONE_EXISTED)
     *                      - role not found (ErrorCode.ROLE_NOT_EXISTED)
     *                      - database error occurs (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Encodes password, assigns USER_ROLE, saves user, sends OTPs for email/phone verification.
     */
    @Override
    @Transactional
    public UserResponse registerUser(RegisterRequest registerRequest) {
        // Map request to entity; assumes full mapping of fields
        User user = userMapper.fromRegisterRequest(registerRequest);
        // Encode password; ensures secure storage
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        // Check for duplicate email; enforces uniqueness
        if (userRepository.existsByEmail(registerRequest.getEmail()))
            throw new AppException(ErrorCode.USER_EXISTED);
        // Check for duplicate phone; enforces uniqueness
        if (userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
            throw new AppException(ErrorCode.PHONE_EXISTED);
        }

        // Fetch default role; fails if not found
        Role role = roleRepository.findByName(PredefinedRole.USER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        try {
            // Set initial user properties; prepares for persistence
            user.setRoles(roles);
            user.setVerificationStatus(VerificationStatus.UNVERIFIED);
            user.setDeleted(false);
            user = userRepository.save(user); // Persist user

            // Generate and store email OTP; 5-minute expiration
            String otpEmail = commonUtil.generateOTP();
            Instant expiredAtEmail = Instant.now().plus(Duration.ofMinutes(5));
            OtpVerification otpVerificationEmail = OtpVerification.builder()
                    .email(registerRequest.getEmail())
                    .otp(otpEmail)
                    .expiredAt(expiredAtEmail)
                    .type(OtpType.VERIFY_EMAIL)
                    .build();
            otpVerificationService.createOtp(otpVerificationEmail, false); // Email-based OTP

            // Send email verification; assumes async delivery
            mailService.sendEmailVerify(registerRequest.getFullName(), otpEmail, registerRequest.getEmail());

            // Generate and store phone OTP; 5-minute expiration
            String otpPhone = commonUtil.generateOTP();
            Instant expiredAtPhone = Instant.now().plus(Duration.ofMinutes(5));
            OtpVerification otpVerificationPhone = OtpVerification.builder()
                    .phoneNumber(registerRequest.getPhoneNumber())
                    .otp(otpPhone)
                    .expiredAt(expiredAtPhone)
                    .type(OtpType.VERIFY_PHONE)
                    .build();
            otpVerificationService.createOtp(otpVerificationPhone, true); // Phone-based OTP

            // Map to response DTO; includes user details
            UserResponse userResponse = userMapper.toUserResponse(user);
            // Log registration action; captures new user state
            activityLogService.logAction(
                    user.getId(),
                    user.getEmail(),
                    UserAction.REGISTER,
                    "User registered success with email: " + user.getEmail(),
                    null,
                    userResponse
            );
            return userResponse;
        } catch (Exception e) {
            // Log error for debugging; generic exception catch
            log.error("Error when update: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Verifies a user's email using an OTP.
     *
     * @param otp   the one-time password sent to the user's email
     * @param email the email address to verify
     * @return true if verification succeeds, false otherwise
     * @throws AppException if user not found (ErrorCode.USER_NOT_EXISTED)
     * @implNote Updates verification status based on current state and logs the action.
     */
    @Override
    public boolean verifyEmail(String otp, String email) {
        // Verify OTP; delegates to service, expects email-based check
        boolean rs = otpVerificationService.verifyOtp(otp, email, OtpType.VERIFY_EMAIL, false);
        // Fetch user; fails if not found
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        // Update verification status; handles partial or full verification
        if (user.getVerificationStatus() != VerificationStatus.UNVERIFIED) {
            user.setVerificationStatus(VerificationStatus.FULLY_VERIFIED); // Email + phone verified
        } else {
            user.setVerificationStatus(VerificationStatus.EMAIL_VERIFIED); // Only email verified
        }
        userRepository.save(user); // Persist updated status
        // Log verification action
        activityLogService.logAction(
                user.getId(),
                user.getEmail(),
                UserAction.EMAIL_VERIFICATION,
                "User verify email success with email: " + user.getEmail(),
                null,
                null
        );
        return rs;
    }

    /**
     * Verifies a user's phone number using an OTP.
     *
     * @param otp   the one-time password sent to the user's phone
     * @param phone the phone number to verify
     * @return true if verification succeeds, false otherwise
     * @throws AppException if user not found (ErrorCode.USER_NOT_EXISTED)
     * @implNote Updates verification status based on current state and logs the action.
     */
    @Override
    public boolean verifyPhone(String otp, String phone) {
        // Verify OTP; delegates to service, expects phone-based check
        boolean rs = otpVerificationService.verifyOtp(otp, phone, OtpType.VERIFY_PHONE, true);
        // Fetch user; fails if not found
        User user = userRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        // Update verification status; handles partial or full verification
        if (user.getVerificationStatus() != VerificationStatus.UNVERIFIED) {
            user.setVerificationStatus(VerificationStatus.FULLY_VERIFIED); // Phone + email verified
        } else {
            user.setVerificationStatus(VerificationStatus.PHONE_VERIFIED); // Only phone verified
        }
        userRepository.save(user); // Persist updated status
        // Log verification action
        activityLogService.logAction(
                user.getId(),
                user.getEmail(),
                UserAction.PHONE_VERIFICATION,
                "User verify phone success with phone: " + user.getPhoneNumber(),
                null,
                null
        );
        return rs;
    }

    /**
     * Verifies an email change request using an OTP and updates the user's email.
     *
     * @param changeMailRequest the {@link VerifyChangeMailRequest} containing:
     *                          - otp: one-time password
     *                          - oldEmail: current email
     *                          - newEmail: desired new email
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - old email doesn't match (ErrorCode.USER_NOT_EXISTED)
     *                      - user not found (ErrorCode.USER_NOT_EXISTED)
     *                      - OTP invalid (via OtpVerificationService)
     *                      - database error occurs (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Updates email, clears login details, and logs out the user.
     */
    @Override
    public void verifyChangeEmail(VerifyChangeMailRequest changeMailRequest) {
        // Check authentication; ensures user is logged in
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Validate old email; ensures request matches authenticated user
        String currentEmail = authentication.getName();
        if (!currentEmail.equals(changeMailRequest.getOldEmail())) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        // Verify OTP; checks validity for new email
        otpVerificationService.verifyOtp(changeMailRequest.getOtp(), changeMailRequest.getNewEmail(), OtpType.CHANGE_EMAIL, false);

        // Fetch user; fails if not found
        User user = userRepository.findByEmail(changeMailRequest.getOldEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        try {
            // Update email; applies new email
            user.setEmail(changeMailRequest.getNewEmail());
            // Clear login details; forces re-authentication with new email
            loginDetailService.deleteLoginDetailByUser(user);
            userRepository.save(user); // Persist changes

            // Log email change action
            activityLogService.logAction(
                    user.getId(),
                    user.getEmail(),
                    UserAction.EMAIL_VERIFICATION,
                    "User verify change email success with email: " + changeMailRequest.getNewEmail(),
                    null,
                    null
            );
            // Clear security context; logs user out due to email change
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            log.error("Error while verifying change email success with email");
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Initiates an email change by generating an OTP and sending it to the new email.
     *
     * @param cMailRequest the {@link ChangeMailRequest} containing:
     *                     - oldEmail: current email
     *                     - newEmail: desired new email
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - old email doesn't match (ErrorCode.USER_NOT_EXISTED)
     *                      - new email already exists (ErrorCode.MAIL_EXISTED)
     *                      - user not found (ErrorCode.USER_NOT_EXISTED)
     * @implNote Sends OTP to new email and notifies old email of the change request.
     */
    @Override
    public void changeEmail(ChangeMailRequest cMailRequest) {
        // Check authentication; ensures user is logged in
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Validate old email; ensures request matches authenticated user
        String currentEmail = authentication.getName();
        if (!currentEmail.equals(cMailRequest.getOldEmail())) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        // Check new email availability; prevents duplicates
        if (userRepository.existsByEmail(cMailRequest.getNewEmail())) {
            throw new AppException(ErrorCode.MAIL_EXISTED);
        }

        // Fetch user; fails if not found
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Generate OTP; 5-minute expiration
        String otp = commonUtil.generateOTP();
        Instant expiredAt = Instant.now().plus(Duration.ofMinutes(5));
        OtpVerification otpVerificationPhone = OtpVerification.builder()
                .email(cMailRequest.getNewEmail())
                .otp(otp)
                .expiredAt(expiredAt)
                .type(OtpType.CHANGE_EMAIL)
                .build();
        otpVerificationService.createOtp(otpVerificationPhone, false); // Email-based OTP

        // Log change request; captures old and new email
        activityLogService.logAction(
                user.getId(),
                user.getEmail(),
                UserAction.CHANGED_EMAIL,
                "User require change email success with email: " + cMailRequest.getNewEmail(),
                cMailRequest.getOldEmail(),
                cMailRequest.getNewEmail()
        );

        // Send OTP to new email; assumes async delivery
        mailService.sendEmailOTP(otp, cMailRequest.getNewEmail(), false, user.getFullName());
        // Notify old email; security measure for unauthorized requests
        mailService.sendSimpleEmail(
                cMailRequest.getOldEmail(),
                "Thông báo: Tài khoản yêu cầu đổi email",
                "Tài khoản của bạn đã yêu cầu đổi email. Nếu không phải bạn, vui lòng liên hệ với chúng tôi."
        );
    }

    /**
     * Verifies a phone number change request using an OTP and updates the user's phone.
     *
     * @param request the {@link VerifyChangePhoneRequest} containing:
     *                - otp: one-time password
     *                - oldPhoneNumber: current phone number
     *                - newPhoneNumber: desired new phone number
     * @throws AppException if:
     *                      - old phone doesn't match (ErrorCode.OLD_PHONE_INVALID)
     *                      - OTP invalid (via OtpVerificationService)
     * @implNote Updates phone number and logs the action.
     */
    @Override
    public void verifyChangePhone(VerifyChangePhoneRequest request) {
        // Verify OTP; checks validity for new phone
        otpVerificationService.verifyOtp(request.getOtp(), request.getNewPhoneNumber(), OtpType.CHANGE_PHONE, true);

        // Fetch authenticated user; ensures valid session
        User user = getAuthenticatedUser();

        // Validate old phone; ensures request matches current phone
        if (!user.getPhoneNumber().equals(request.getOldPhoneNumber())) {
            throw new AppException(ErrorCode.OLD_PHONE_INVALID);
        }

        // Update phone number; applies new value
        user.setPhoneNumber(request.getNewPhoneNumber());
        userRepository.save(user); // Persist changes

        // Log phone change action
        activityLogService.logAction(
                user.getId(),
                user.getEmail(),
                UserAction.PHONE_VERIFICATION,
                "User verify change phone success with phone: " + request.getNewPhoneNumber(),
                null,
                null
        );
    }

    /**
     * Initiates a phone number change by generating an OTP and sending it to the new phone.
     *
     * @param request the {@link ChangePhoneRequest} containing:
     *                - oldPhoneNumber: current phone number
     *                - newPhoneNumber: desired new phone number
     * @throws AppException if:
     *                      - old phone doesn't match (ErrorCode.OLD_PHONE_NOT_EXISTED)
     *                      - new phone already exists (ErrorCode.PHONE_EXISTED)
     * @implNote Generates OTP for new phone and logs the request.
     */
    @Override
    public void changePhone(ChangePhoneRequest request) {
        // Fetch authenticated user; ensures valid session
        User user = getAuthenticatedUser();
        // Validate old phone; ensures request matches current phone
        if (!user.getPhoneNumber().equals(request.getOldPhoneNumber())) {
            throw new AppException(ErrorCode.OLD_PHONE_NOT_EXISTED);
        }

        // Check new phone availability; prevents duplicates
        if (userRepository.existsByPhoneNumber(request.getNewPhoneNumber())) {
            throw new AppException(ErrorCode.PHONE_EXISTED);
        }

        // Generate OTP; 5-minute expiration
        String otp = commonUtil.generateOTP();
        Instant expiredAt = Instant.now().plus(Duration.ofMinutes(5));
        OtpVerification otpVerificationPhone = OtpVerification.builder()
                .phoneNumber(request.getNewPhoneNumber())
                .otp(otp)
                .expiredAt(expiredAt)
                .type(OtpType.CHANGE_PHONE)
                .build();
        otpVerificationService.createOtp(otpVerificationPhone, true); // Phone-based OTP

        // Log change request; captures old and new phone
        activityLogService.logAction(
                user.getId(),
                user.getEmail(),
                UserAction.CHANGED_PHONE,
                "User require change phone success with phone: " + request.getNewPhoneNumber(),
                request.getOldPhoneNumber(),
                request.getNewPhoneNumber()
        );
    }

    /**
     * Retrieves the authenticated user from the security context.
     *
     * @return the {@link User} entity of the authenticated user
     * @throws AppException if:
     *                      - authentication missing (ErrorCode.UNAUTHORIZED)
     *                      - user not found (ErrorCode.USER_NOT_EXISTED)
     * @implNote Uses JWT principal (email) to fetch user; assumes email uniqueness.
     */
    private User getAuthenticatedUser() {
        // Access security context; assumes JWT-based authentication
        SecurityContext jwtContext = SecurityContextHolder.getContext();
        // Validate authentication; fails fast if invalid
        if (jwtContext == null || jwtContext.getAuthentication() == null ||
                !jwtContext.getAuthentication().isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        // Log principal for debugging; assumes email as subject
        log.info("Authentication {}", jwtContext.getAuthentication().getName());

        // Fetch user by email; assumes reliable identifier
        String email = jwtContext.getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    /**
     * Retrieves the roles associated with a user from a JWT token.
     *
     * @param token    the JWT token containing user claims
     * @param response the {@link HttpServletResponse} (unused in current impl)
     * @return a {@link List} of role names as strings
     * @throws AppException if token parsing fails (ErrorCode.UNAUTHENTICATED)
     * @implNote Extracts "scope" claim from token and splits into role list.
     */
    @Override
    public List<String> getRolesUser(String token, HttpServletResponse response) {
        // Verify token; ensures validity and integrity
        SignedJWT signedJWT = jwtTokenProvider.verifyToken(token);

        try {
            // Extract claims; contains user metadata
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            // Get scope claim; assumes space-separated roles
            String scope = claimsSet.getStringClaim("scope");
            // Split scope into list; returns empty list if null
            return scope != null ? Arrays.asList(scope.split(" ")) : List.of();
        } catch (ParseException e) {
            // Log parsing error; indicates malformed token
            log.error("Error parsing token claims", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }
}