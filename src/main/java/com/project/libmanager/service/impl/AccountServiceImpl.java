package com.project.libmanager.service.impl;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.libmanager.constant.*;
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
import com.project.libmanager.service.IOtpVerificationService;
import com.project.libmanager.service.dto.request.*;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements IAccountService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final IMailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final IOtpVerificationService otpVerificationService;
    private final IActivityLogService activityLogService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CommonUtil commonUtil;
    /**
     * Registers a new user by saving their details, encoding the password,
     * assigning roles,
     * and sending a verification email.
     *
     * @param registerRequest the request containing user registration details.
     * @return the response containing user information.
     * @throws AppException if the user already exists, the role is not found, or an
     *                      unexpected error occurs.
     * @implNote This method maps the registration request to a User entity, encodes
     * the password,
     * checks for duplicate emails, assigns the default role, saves the
     * user, generates a verification token,
     * and sends a verification email.
     */
    @Override
    @Transactional
    public UserResponse registerUser(RegisterRequest registerRequest) {
        User user = userMapper.fromRegisterRequest(registerRequest);

        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        if (userRepository.existsByEmail(registerRequest.getEmail()))
            throw new AppException(ErrorCode.USER_EXISTED);

        if(userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
            throw new AppException(ErrorCode.PHONE_EXISTED);
        }

        Role role = roleRepository.findByName(PredefinedRole.USER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        try {
            user.setRoles(roles);
            user.setVerificationStatus(VerificationStatus.UNVERIFIED);
            user.setDeleted(false);
            user.setResetPassword(false);
            user = userRepository.save(user);

            String otpEmail = commonUtil.generateOTP();
            Instant expiredAtEmail = Instant.now().plus(Duration.ofMinutes(5));
            OtpVerification otpVerificationEmail = OtpVerification.builder()
                    .email(registerRequest.getEmail())
                    .otp(otpEmail)
                    .expiredAt(expiredAtEmail)
                    .type(OtpType.VERIFY_EMAIL)
                    .build();
            otpVerificationService.createOtp(otpVerificationEmail, false);

            // send email verify
            mailService.sendEmailVerify(registerRequest.getFullName(), otpEmail, registerRequest.getEmail());

            String otpPhone = commonUtil.generateOTP();
            Instant expiredAtPhone = Instant.now().plus(Duration.ofMinutes(5));
            OtpVerification otpVerificationPhone = OtpVerification.builder()
                    .phoneNumber(registerRequest.getPhoneNumber())
                    .otp(otpPhone)
                    .expiredAt(expiredAtPhone)
                    .type(OtpType.VERIFY_PHONE)
                    .build();
            otpVerificationService.createOtp(otpVerificationPhone, true);

            UserResponse userResponse = userMapper.toUserResponse(user);
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
            log.error("Error when update: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Verifies the user's email through a token.
     *
     * @param otp The token used to verify the email.
     * @param email The email used to verify.
     * @return true if the email was successfully verified, false otherwise.
     * @throws AppException   If the user does not exist or cannot be authenticated.
     * @implNote This method checks the validity of the token and uses it to verify
     * the user's email.
     */
    @Override
    public boolean verifyEmail(String otp, String email) {
        boolean rs = otpVerificationService.verifyOtp(otp, email, OtpType.VERIFY_EMAIL, false);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (user.getVerificationStatus() != VerificationStatus.UNVERIFIED) {
            user.setVerificationStatus(VerificationStatus.FULLY_VERIFIED);
        } else {
            user.setVerificationStatus(VerificationStatus.EMAIL_VERIFIED);
        }
        userRepository.save(user);
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

    @Override
    public boolean verifyPhone(String otp, String phone) {
        boolean rs = otpVerificationService.verifyOtp(otp, phone, OtpType.VERIFY_PHONE, true);
        User user = userRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (user.getVerificationStatus() != VerificationStatus.UNVERIFIED) {
            user.setVerificationStatus(VerificationStatus.FULLY_VERIFIED);
        } else {
            user.setVerificationStatus(VerificationStatus.PHONE_VERIFIED);
        }
        userRepository.save(user);
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
     * Verifies an email change request using an OTP.
     *
     * @param changeMailRequest the request containing the OTP and email details.
     * @throws AppException if the OTP does not exist, is expired, or the user does
     *                      not exist.
     * @implNote Checks the validity of the OTP and updates the user's email if
     * valid.
     */
    @Override
    public void verifyChangeEmail(VerifyChangeMailRequest changeMailRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String currentEmail = authentication.getName();
        if (!currentEmail.equals(changeMailRequest.getOldEmail())) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        otpVerificationService.verifyOtp(changeMailRequest.getOtp(), changeMailRequest.getNewEmail(),OtpType.CHANGE_EMAIL,false );

        User user = userRepository.findByEmail(changeMailRequest.getOldEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setEmail(changeMailRequest.getNewEmail());
        userRepository.save(user);

        activityLogService.logAction(
                user.getId(),
                user.getEmail(),
                UserAction.EMAIL_VERIFICATION,
                "User verify change email success with email: " + changeMailRequest.getNewEmail(),
                null,
                null
        );
    }

    /**
     * Initiates an email change process by sending an OTP to the new email.
     *
     * @param cMailRequest the request containing old and new email addresses.
     * @throws AppException if the user is not authenticated or does not exist.
     * @implNote Validates the old email, generates an OTP, and sends it to the new
     * email address.
     */
    @Override
    public void changeEmail(ChangeMailRequest cMailRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String currentEmail = authentication.getName();
        if (!currentEmail.equals(cMailRequest.getOldEmail())) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        if (userRepository.existsByEmail(cMailRequest.getNewEmail())) {
            throw new AppException(ErrorCode.MAIL_EXISTED);
        }

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String otp = commonUtil.generateOTP();
        Instant expiredAt = Instant.now().plus(Duration.ofMinutes(5));

        OtpVerification otpVerificationPhone = OtpVerification.builder()
                .email(cMailRequest.getNewEmail())
                .otp(otp)
                .expiredAt(expiredAt)
                .type(OtpType.CHANGE_EMAIL)
                .build();

        otpVerificationService.createOtp(otpVerificationPhone, false);

        activityLogService.logAction(
                user.getId(),
                user.getEmail(),
                UserAction.CHANGED_EMAIL,
                "User require change email success with email: " + cMailRequest.getNewEmail(),
                cMailRequest.getOldEmail(),
                cMailRequest.getNewEmail()
        );

        mailService.sendEmailOTP(otp, cMailRequest.getNewEmail(), false, user.getFullName());
        mailService.sendSimpleEmail(
                cMailRequest.getOldEmail(),
                "Thông báo: Tài khoản yêu cầu đổi email",
                "Tài khoản của bạn đã yêu cầu đổi email. Nếu không phải bạn, vui lòng liên hệ với chúng tôi."
        );
    }

    @Override
    public void verifyChangePhone(VerifyChangePhoneRequest request) {
        otpVerificationService.verifyOtp(request.getOtp(), request.getNewPhoneNumber(),OtpType.CHANGE_PHONE,true );

        User user = getAuthenticatedUser();

        if(!user.getPhoneNumber().equals(request.getOldPhoneNumber())) {
            throw new AppException(ErrorCode.OLD_PHONE_INVALID);
        }

        user.setPhoneNumber(request.getNewPhoneNumber());
        userRepository.save(user);

        activityLogService.logAction(
                user.getId(),
                user.getEmail(),
                UserAction.PHONE_VERIFICATION,
                "User verify change phone success with phone: " + request.getNewPhoneNumber(),
                null,
                null
        );
    }

    @Override
    public void changePhone(ChangePhoneRequest request) {
        User user = getAuthenticatedUser();
        if(!user.getPhoneNumber().equals(request.getOldPhoneNumber())) {
            throw new AppException(ErrorCode.OLD_PHONE_NOT_EXISTED);
        }

        if (userRepository.existsByPhoneNumber(request.getNewPhoneNumber())) {
            throw new AppException(ErrorCode.PHONE_EXISTED);
        }

        String otp = commonUtil.generateOTP();
        Instant expiredAt = Instant.now().plus(Duration.ofMinutes(5));

        OtpVerification otpVerificationPhone = OtpVerification.builder()
                .phoneNumber(request.getNewPhoneNumber())
                .otp(otp)
                .expiredAt(expiredAt)
                .type(OtpType.CHANGE_PHONE)
                .build();

        otpVerificationService.createOtp(otpVerificationPhone, true);

        activityLogService.logAction(
                user.getId(),
                user.getEmail(),
                UserAction.CHANGED_PHONE,
                "User require change phone success with phone: " + request.getNewPhoneNumber(),
                request.getOldPhoneNumber(),
                request.getNewPhoneNumber()
        );
    }

    private User getAuthenticatedUser() {
        SecurityContext jwtContext = SecurityContextHolder.getContext();
        if (jwtContext == null || jwtContext.getAuthentication() == null ||
                !jwtContext.getAuthentication().isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        log.info("Authentication {}", jwtContext.getAuthentication().getName());

        String email = jwtContext.getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    @Override
    public List<String> getRolesUser(String token, HttpServletResponse response) {
        JWTClaimsSet claimsSet;
        SignedJWT signedJWT = jwtTokenProvider.verifyToken(token, false);

        try {
            claimsSet = signedJWT.getJWTClaimsSet();
            String scope = claimsSet.getStringClaim("scope");
            return scope != null ? Arrays.asList(scope.split(" ")) : List.of();
        } catch (ParseException e) {
            log.error("Error parsing token claims", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

}
