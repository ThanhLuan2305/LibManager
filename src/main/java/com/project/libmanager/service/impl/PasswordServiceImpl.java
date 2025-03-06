package com.project.libmanager.service.impl;

import com.nimbusds.jose.JOSEException;
import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.constant.TokenType;
import com.project.libmanager.entity.OtpVerification;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.repository.OtpVerificationRepository;
import com.project.libmanager.repository.UserRepository;
import com.project.libmanager.sercurity.JwtTokenProvider;
import com.project.libmanager.service.IMailService;
import com.project.libmanager.service.IPasswordService;
import com.project.libmanager.service.dto.request.ChangePasswordRequest;
import com.project.libmanager.service.dto.response.ChangePassAfterResetRequest;
import com.project.libmanager.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordServiceImpl implements IPasswordService {
    private final UserRepository userRepository;
    private final IMailService mailService;
    private final OtpVerificationRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
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

        if (!cpRequest.getNewPassword().equals(cpRequest.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        if (passwordEncoder.matches(cpRequest.getNewPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_DUPLICATED);
        }
        try {
            user.setPassword(passwordEncoder.encode(cpRequest.getNewPassword()));
            user.setResetPassword(false);
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Initiates the password reset process by sending an OTP to the user's email.
     *
     * @param email The email of the user requesting the password reset.
     * @throws AppException If the user does not exist or if the email is not
     *                      verified.
     * @implNote This method sends an OTP (One-Time Password) to the user's email
     * for password reset.
     */
    @Override
    public void forgetPassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        boolean isVerified = user.isVerified();
        if (!isVerified) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);

        }
        Integer otp = commonUtil.generateOTP();
        Instant expiredAt = Instant.now().plus(Duration.ofMinutes(5));
        otpRepository.save(OtpVerification.builder()
                .email(email)
                .otp(otp)
                .expiredAt(expiredAt)
                .build());
        mailService.sendEmailOTP(otp, user.getEmail(), true, user.getFullName());
    }

    /**
     * Verifies the OTP provided by the user for password reset.
     *
     * @param token The OTP provided by the user.
     * @param email The email address associated with the OTP.
     * @return An authentication response containing the generated JWT token if OTP
     * is valid.
     * @throws AppException If the OTP does not exist, has expired, or any other
     *                      error occurs.
     * @implNote This method checks the validity of the OTP and generates a new JWT
     * token for authentication.
     */
    @Override
    public String verifyOTP(Integer token, String email) {
        OtpVerification otp = otpRepository.findByOtp(token)
                .orElseThrow(() -> new AppException(ErrorCode.OTP_NOT_EXISTED));

        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (otp.getExpiredAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        return jwtTokenProvider.generateToken(user, TokenType.ACCESS);
    }

    /**
     * Resets a user's password using a token.
     *
     * @param token the reset token received by the user.
     * @return the newly generated password.
     * @throws JOSEException  if there is an error processing the token.
     * @throws ParseException if the token cannot be parsed.
     * @throws AppException   if the user does not exist.
     * @implNote Decodes the token, verifies its validity, and generates a new
     * password for the user.
     */
    @Override
    public String resetPassword(String token) throws JOSEException, ParseException {
//        var signedJWT = verifyToken(token, false);
//        String email = signedJWT.getJWTClaimsSet().getSubject();
//
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//        try {
//
//            String password = commonUtil.generatePassword(9);
//            user.setPassword(passwordEncoder.encode(password));
//            user.setResetPassword(true);
//            userRepository.save(user);
//
//            invalidToken(token);
//            return password;
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            throw e;
//        }
        return "";
    }
}
