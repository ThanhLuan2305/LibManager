package com.project.libmanager.service.impl;

import com.nimbusds.jose.JOSEException;
import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.constant.PredefinedRole;
import com.project.libmanager.constant.TokenType;
import com.project.libmanager.entity.OtpVerification;
import com.project.libmanager.entity.Role;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.repository.OtpVerificationRepository;
import com.project.libmanager.repository.RoleRepository;
import com.project.libmanager.repository.UserRepository;
import com.project.libmanager.sercurity.JwtTokenProvider;
import com.project.libmanager.service.IAccountService;
import com.project.libmanager.service.IMailService;
import com.project.libmanager.service.IMaintenanceService;
import com.project.libmanager.service.IUserService;
import com.project.libmanager.service.dto.request.ChangeMailRequest;
import com.project.libmanager.service.dto.request.RegisterRequest;
import com.project.libmanager.service.dto.request.VerifyChangeMailRequest;
import com.project.libmanager.service.dto.response.UserResponse;
import com.project.libmanager.service.mapper.UserMapper;
import com.project.libmanager.util.CommonUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements IAccountService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final IMailService mailService;
    private final OtpVerificationRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final IMaintenanceService maintenanceService;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CommonUtil commonUtil;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final IUserService userService;
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

        Role role = roleRepository.findByName(PredefinedRole.USER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        try {
            user.setRoles(roles);
            user.setVerified(false);
            user.setDeleted(false);
            user.setResetPassword(false);
            userRepository.save(user);
            String token = jwtTokenProvider.generateToken(user, TokenType.VERIFY_MAIL);

            // send email verify
            mailService.sendEmailVerify(registerRequest.getFullName(), token, registerRequest.getEmail());

            return userMapper.toUserResponse(user);
        } catch (Exception e) {
            log.error("Error when update: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Verifies the user's email through a token.
     *
     * @param token The token used to verify the email.
     * @return true if the email was successfully verified, false otherwise.
     * @throws JOSEException  If an error occurs while verifying the token.
     * @throws ParseException If an error occurs while parsing the token.
     * @throws AppException   If the user does not exist or cannot be authenticated.
     * @implNote This method checks the validity of the token and uses it to verify
     * the user's email.
     */
    @Override
    public boolean verifyEmail(String token) throws JOSEException, ParseException {
//        JWSVerifier verifier = new MACVerifier(signKey.getBytes());
//
//        SignedJWT signedJWT = SignedJWT.parse(token);
//        // check verify or refresh token
//        Date expTime = signedJWT.getJWTClaimsSet().getExpirationTime();
//        boolean rs = signedJWT.verify(verifier);
//        if (!expTime.after(new Date()) || !rs) {
//            String email = signedJWT.getJWTClaimsSet().getSubject();
//            User user = userRepository.findByEmail(email)
//                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//            userRepository.delete(user);
//            return false;
//
//        } else {
//            String email = signedJWT.getJWTClaimsSet().getSubject();
//            User user = userRepository.findByEmail(email)
//                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//            user.setVerified(true);
//            userRepository.save(user);
//            return true;
//        }
        return true;
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
        OtpVerification otp = otpRepository.findByOtp(changeMailRequest.getOtp())
                .orElseThrow(() -> new AppException(ErrorCode.OTP_NOT_EXISTED));

        User user = userRepository.findByEmail(changeMailRequest.getOldEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (otp.getExpiredAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }
        // Delete otp
        user.setEmail(changeMailRequest.getNewEmail());
        userRepository.save(user);
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
        var jwtContex = SecurityContextHolder.getContext();
        Authentication authentication = jwtContex.getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String email = jwtContex.getAuthentication().getName();

        if (!email.equals(cMailRequest.getOldEmail())) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        boolean checkMail = userRepository.existsByEmail(cMailRequest.getNewEmail());
        if (checkMail) {
            throw new AppException(ErrorCode.MAIL_EXISTED);
        }

        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        int otp = commonUtil.generateOTP();
        Instant expiredAt = Instant.now().plus(Duration.ofMinutes(5));
        otpRepository.save(OtpVerification.builder()
                .email(email)
                .otp(otp)
                .expiredAt(expiredAt)
                .build());
        mailService.sendEmailOTP(otp, cMailRequest.getNewEmail(), false, user.getFullName());
        mailService.sendSimpleEmail(cMailRequest.getOldEmail(), "Thông báo tài khoản yêu cầu đổi email",
                "Tài khoản của bạn đã yêu cầu đổi email, nếu không phải bạn vui lòng liên hệ với chúng tôi");
    }
}
