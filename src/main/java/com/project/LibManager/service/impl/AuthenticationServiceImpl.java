package com.project.LibManager.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.project.LibManager.constant.ErrorCode;
import com.project.LibManager.constant.PredefinedRole;
import com.project.LibManager.constant.TokenType;
import com.project.LibManager.service.dto.request.AuthenticationRequest;
import com.project.LibManager.service.dto.request.LogoutRequest;
import com.project.LibManager.service.dto.request.RegisterRequest;
import com.project.LibManager.service.dto.request.TokenRequest;
import com.project.LibManager.service.dto.request.ChangePasswordRequest;
import com.project.LibManager.service.dto.request.VerifyChangeMailRequest;
import com.project.LibManager.service.dto.request.ChangeMailRequest;
import com.project.LibManager.service.dto.response.AuthenticationResponse;
import com.project.LibManager.service.dto.response.ChangePassAfterResetRequest;
import com.project.LibManager.service.dto.response.IntrospectResponse;
import com.project.LibManager.service.dto.response.UserResponse;
import com.project.LibManager.entity.InvalidateToken;
import com.project.LibManager.entity.OtpVerification;
import com.project.LibManager.entity.Role;
import com.project.LibManager.entity.User;
import com.project.LibManager.exception.AppException;
import com.project.LibManager.repository.InvalidateTokenRepository;
import com.project.LibManager.repository.OtpVerificationRepository;
import com.project.LibManager.repository.RoleRepository;
import com.project.LibManager.repository.UserRepository;
import com.project.LibManager.sercurity.JwtTokenProvider;
import com.project.LibManager.service.IAuthenticationService;
import com.project.LibManager.service.IMailService;
import com.project.LibManager.service.IMaintenanceService;
import com.project.LibManager.service.mapper.UserMapper;
import com.project.LibManager.util.CommonUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements IAuthenticationService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final InvalidateTokenRepository invalidateTokenRepository;
    private final IMailService mailService;
    private final OtpVerificationRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final IMaintenanceService maintenanceService;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CommonUtil commonUtil;

    @Value("${jwt.signing.key}")
    private String signKey;

    @Value("${jwt.refresh-duration}")
    private Long refreshDuration;


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
    public AuthenticationResponse authenticate(AuthenticationRequest aRequest) {
        User user = userRepository.findByEmail(aRequest.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        boolean isVerified = user.isVerified();
        boolean isDeleted = user.isDeleted();
        boolean isResetPass = user.isResetPassword();
        Role role = roleRepository.findByName(PredefinedRole.USER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        boolean rs = passwordEncoder.matches(aRequest.getPassword(), user.getPassword());

        if (!isVerified)
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);

        if (isDeleted) {
            throw new AppException(ErrorCode.USER_IS_DELETED);
        }

        if (maintenanceService.isMaintenanceMode() && user.getRoles().contains(role)) {
            throw new AppException(ErrorCode.MAINTENACE_MODE);
        }

        if (!rs)
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);

        if (isResetPass) {
            return AuthenticationResponse.builder().forceChangePassword(user.isResetPassword()).build();
        }

        String accessToken = jwtTokenProvider.generateToken(user, TokenType.ACCESS);
        String refreshToken = jwtTokenProvider.generateToken(user, TokenType.REFRESH);

        return AuthenticationResponse.builder().accessToken(accessToken).refreshToken(refreshToken)
                .forceChangePassword(user.isResetPassword()).build();
    }

    /**
     * Logs out a user by invalidating the given token.
     *
     * @param iRequest the token request containing the token to be invalidated.
     * @throws AppException if the token is already expired or invalid.
     * @implNote Marks the token as invalid in the token store or database.
     */
    @Override
    public IntrospectResponse introspectToken(TokenRequest iRequest) {
        String token = iRequest.getToken();
        boolean invalid = true;

        try {
            verifyToken(token, false);
            return IntrospectResponse.builder().valid(invalid).build();
        } catch (Exception e) {
            log.error(token, e.getMessage());
            throw new AppException(ErrorCode.JWT_TOKEN_INVALID);
        }

    }

    /**
     * Invalidates a given JWT token by storing it in the database.
     *
     * @param token The JWT token to be invalidated.
     * @throws ParseException If there is an error parsing the token.
     * @throws JOSEException  If there is an error during the token's cryptographic verification.
     * @implNote This method verifies the token, extracts its unique ID and expiration time,
     * and saves it to the database to prevent further use.
     */
    private void invalidToken(String token) throws ParseException, JOSEException {
        var sigToken = verifyToken(token, false);

        String jwtID = sigToken.getJWTClaimsSet().getJWTID();
        Instant expTime = sigToken.getJWTClaimsSet().getExpirationTime().toInstant();
        InvalidateToken invalidateToken = InvalidateToken.builder()
                .id(jwtID)
                .expiryTime(expTime)
                .build();

        invalidateTokenRepository.save(invalidateToken);
    }

    /**
     * Logs out a user by invalidating their access and refresh tokens.
     *
     * @param logoutRequest The request containing the access and refresh tokens to be invalidated.
     * @throws ParseException If there is an error parsing the tokens.
     * @throws JOSEException  If there is an error with the token's cryptographic operations.
     * @throws AppException   If the tokens are already expired or if there is a failure during logout.
     */
    @Override
    public void logout(LogoutRequest logoutRequest) throws ParseException, JOSEException {
        try {
            invalidToken(logoutRequest.getAccessToken());
            invalidToken(logoutRequest.getRefreshToken());
        } catch (AppException e) {
            log.info("Token already expired");
            throw new AppException(ErrorCode.LOGOUT_FAIL);
        }
    }

    /**
     * Verifies the provided JWT token, checks its expiration time,
     * and ensures it has not been invalidated.
     *
     * @param token     The JWT token to verify.
     * @param isRefresh Indicates whether the token is a refresh token.
     * @return The parsed and verified SignedJWT.
     * @throws JOSEException  If there is an error during token signature verification.
     * @throws ParseException If the token cannot be parsed.
     * @throws AppException   If the token is expired, invalid, or has been revoked.
     * @implNote This method parses the JWT, verifies its signature, checks its expiration time,
     * and ensures it is not present in the invalidated token repository.
     */
    @Override
    public SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(signKey.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);
        // check verify or refresh token
        Date expTime = (isRefresh) ? new Date(signedJWT.getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(refreshDuration, ChronoUnit.SECONDS)
                .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean rs = signedJWT.verify(verifier);

        if (!expTime.after(new Date())) {
            log.error("Token expired");
            throw new AppException(ErrorCode.JWT_TOKEN_EXPIRED);
        }

        if (!rs) {
            log.error("Token invalid");
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        }

        if (invalidateTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            log.error("Token invalid");
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        }

        return signedJWT;
    }

    /**
     * Refreshes the authentication token by verifying the provided refresh token,
     * invalidating the old token,
     * and generating a new access token and refresh token.
     *
     * @param refreshRequest the request containing the refresh token.
     * @return an AuthenticationResponse containing the new access token and refresh
     * token.
     * @throws JOSEException  if there is an error during JWT processing.
     * @throws ParseException if the refresh token cannot be parsed.
     * @throws AppException   if the user associated with the token does not exist.
     * @implNote This method verifies the refresh token, invalidates the old token
     * by storing it in the database,
     * retrieves the user associated with the token, and generates new
     * authentication tokens.
     */
    @Override
    public AuthenticationResponse refreshToken(TokenRequest refreshRequest) throws JOSEException, ParseException {
        var signedJWT = verifyToken(refreshRequest.getToken(), true);

        String jwtID = signedJWT.getJWTClaimsSet().getJWTID();
        Instant expTime = signedJWT.getJWTClaimsSet().getExpirationTime().toInstant();

        InvalidateToken invalidateToken = InvalidateToken.builder()
                .id(jwtID)
                .expiryTime(expTime)
                .build();
        invalidateTokenRepository.save(invalidateToken);

        String email = signedJWT.getJWTClaimsSet().getSubject();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String accesstoken = jwtTokenProvider.generateToken(user, TokenType.ACCESS);
        String refreshtoken = jwtTokenProvider.generateToken(user, TokenType.REFRESH);
        return AuthenticationResponse.builder().accessToken(accesstoken).refreshToken(refreshtoken)
                .build();
    }

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
        JWSVerifier verifier = new MACVerifier(signKey.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);
        // check verify or refresh token
        Date expTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        boolean rs = signedJWT.verify(verifier);
        if (!expTime.after(new Date()) || !rs) {
            String email = signedJWT.getJWTClaimsSet().getSubject();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            userRepository.delete(user);
            return false;

        } else {
            String email = signedJWT.getJWTClaimsSet().getSubject();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            user.setVerified(true);
            userRepository.save(user);
            return true;
        }
    }

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
        var jwtContex = SecurityContextHolder.getContext();
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
        var signedJWT = verifyToken(token, false);
        String email = signedJWT.getJWTClaimsSet().getSubject();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        try {

            String password = commonUtil.generatePassword(9);
            user.setPassword(passwordEncoder.encode(password));
            user.setResetPassword(true);
            userRepository.save(user);

            invalidToken(token);
            return password;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
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
