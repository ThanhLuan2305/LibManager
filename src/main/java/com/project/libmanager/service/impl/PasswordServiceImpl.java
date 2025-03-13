package com.project.libmanager.service.impl;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.constant.OtpType;
import com.project.libmanager.constant.VerificationStatus;
import com.project.libmanager.entity.OtpVerification;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.repository.UserRepository;
import com.project.libmanager.service.IMailService;
import com.project.libmanager.service.IOtpVerificationService;
import com.project.libmanager.service.IPasswordService;
import com.project.libmanager.service.dto.request.ChangePasswordRequest;
import com.project.libmanager.service.dto.response.ChangePassAfterResetRequest;
import com.project.libmanager.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordServiceImpl implements IPasswordService {
    private final UserRepository userRepository;
    private final IMailService mailService;
    private final IOtpVerificationService otpVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final CommonUtil commonUtil;

    /**
     * Changes the user's password.
     *
     * @param cpRequest The request containing the old password, new password, and
     *                  confirm password.
     * @return true if the password was successfully changed.
     * @throws AppException If there are errors related to the password (e.g.,
     *                      passwords do not match, old password is incorrect, new
     *                      password is the same as the old one).
     * @implNote This method allows the user to change their password, ensuring that
     * the old password is correct and the new password is different from
     * the old one.
     */
    @Override
    public boolean changePassword(ChangePasswordRequest cpRequest) {
        SecurityContext jwtContex = SecurityContextHolder.getContext();
        String email = jwtContex.getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean rs = passwordEncoder.matches(cpRequest.getOldPassword(), user.getPassword());
        if (!rs) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (passwordEncoder.matches(cpRequest.getNewPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_DUPLICATED);
        }

        if (!cpRequest.getNewPassword().equals(cpRequest.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        user.setPassword(passwordEncoder.encode(cpRequest.getNewPassword()));
        userRepository.save(user);
        return true;
    }

    /**
     * Changes the user's password after a password reset request.
     *
     * @param cpRequest the request containing email, new password, and confirmation
     *                  password.
     * @return {@code true} if the password is successfully changed.
     * @throws AppException if the user does not exist, the new password and
     *                      confirmation do not match,
     *                      or the new password is the same as the old password.
     * @implNote This method verifies that the user exists, ensures the new password
     * matches the confirmation password,
     * checks that the new password is not the same as the old password,
     * and updates the password securely.
     */
    @Override
    public boolean changePasswordAfterReset(ChangePassAfterResetRequest cpRequest) {
        User user = userRepository.findByEmail(cpRequest.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(
                new UsernamePasswordAuthenticationToken(cpRequest.getEmail(), cpRequest.getOldPassword())
        );

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (!cpRequest.getNewPassword().equals(cpRequest.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        if (passwordEncoder.matches(cpRequest.getNewPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_DUPLICATED);
        }

        user.setPassword(passwordEncoder.encode(cpRequest.getNewPassword()));
        user.setResetPassword(false);
        userRepository.save(user);

        return true;
    }

    /**
     * Initiates the password reset process by sending an OTP to the user's email.
     *
     * @param contactInfo The contactInfo of the user requesting the password reset.
     * @throws AppException If the user does not exist or if the email is not
     *                      verified.
     * @implNote This method sends an OTP (One-Time Password) to the user's email
     * for password reset.
     */
    @Override
    public void forgetPassword(String contactInfo, boolean isPhone) {

        User user = isPhone ? userRepository.findByPhoneNumber(contactInfo).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED))
                            : userRepository.findByEmail(contactInfo).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (!user.getVerificationStatus().equals(VerificationStatus.FULLY_VERIFIED)) {
            throw new AppException(ErrorCode.USER_NOT_VERIFIED);
        }

        String otp = commonUtil.generateOTP();
        Instant expiredAt = Instant.now().plus(Duration.ofMinutes(5));
        OtpVerification.OtpVerificationBuilder otpBuilder = OtpVerification.builder()
                .otp(otp)
                .expiredAt(expiredAt)
                .type(OtpType.RESET_PASSWORD);
        if(isPhone) {
            otpBuilder.phoneNumber(contactInfo);
        } else {
            otpBuilder.email(contactInfo);
        }
        otpVerificationService.createOtp(otpBuilder.build(), false);
        mailService.sendEmailOTP(otp, user.getEmail(), true, user.getFullName());
    }

    /**
     * Resets a user's password using a token.
     *
     * @param otp the reset otp received by the user.
     * @return the newly generated password.
     * @throws AppException   if the user does not exist.
     * @implNote Decodes the token, verifies its validity, and generates a new
     * password for the user.
     */
    @Override
    public String resetPassword(String otp, String contactInfo, boolean isPhone) {
        otpVerificationService.verifyOtp(otp, contactInfo, OtpType.RESET_PASSWORD, isPhone);

        User user = userRepository.findByEmail(contactInfo)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        try {
            String password = commonUtil.generatePassword(9);
            user.setPassword(passwordEncoder.encode(password));
            user.setResetPassword(true);
            userRepository.save(user);

            return password;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }
}
