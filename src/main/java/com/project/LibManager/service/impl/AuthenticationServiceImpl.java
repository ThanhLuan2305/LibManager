package com.project.LibManager.service.impl;

import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.LibManager.constant.ErrorCode;
import com.project.LibManager.constant.PredefinedRole;
import com.project.LibManager.constant.TokenType;
import com.project.LibManager.dto.request.AuthenticationRequest;
import com.project.LibManager.dto.request.ChangeMailRequest;
import com.project.LibManager.dto.request.ChangePasswordRequest;
import com.project.LibManager.dto.request.LogoutRequest;
import com.project.LibManager.dto.request.RegisterRequest;
import com.project.LibManager.dto.request.TokenRequest;
import com.project.LibManager.dto.request.UserCreateRequest;
import com.project.LibManager.dto.request.VerifyChangeMailRequest;
import com.project.LibManager.dto.response.AuthenticationResponse;
import com.project.LibManager.dto.response.ChangePassAfterResetRequest;
import com.project.LibManager.dto.response.IntrospectResponse;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.entity.InvalidateToken;
import com.project.LibManager.entity.OtpVerification;
import com.project.LibManager.entity.Role;
import com.project.LibManager.entity.User;
import com.project.LibManager.exception.AppException;
import com.project.LibManager.mapper.UserMapper;
import com.project.LibManager.repository.InvalidateTokenRepository;
import com.project.LibManager.repository.OtpVerificationRepository;
import com.project.LibManager.repository.RoleRepository;
import com.project.LibManager.repository.UserRepository;
import com.project.LibManager.service.IAuthenticationService;
import com.project.LibManager.service.IMailService;
import com.project.LibManager.service.IMaintenanceService;
import com.project.LibManager.service.IUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    @Value("${jwt.signing.key}")
    private String SIGN_KEY;

    @Value("${jwt.valid-duration}")
    private Long VALID_DURATION;

    @Value("${jwt.refresh-duration}")
    private Long REFRESH_DURATION;

    @Value("${jwt.mail-duration}")
    private Long MAIL_DURATION; 

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final SecureRandom random = new SecureRandom();

    /**
     * Authenticates a user based on email and password.
     *
     * @param aRequest the authentication request containing email and password.
     * @return an {@link AuthenticationResponse} containing authentication status and token.
     * @throws AppException if the user does not exist, email is not verified, or password is incorrect.
     * @implNote This method verifies user credentials and generates a JWT token upon successful authentication.
     */
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest aRequest) {
        User user = userRepository.findByEmail(aRequest.getEmail()).orElseThrow(() -> 
        new AppException(ErrorCode.USER_NOT_EXISTED));

        if(!user.getIsVerified()) 
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        
        if(user.getIsDeleted()) {
            throw new AppException(ErrorCode.USER_IS_DELETED);
        }
        Role role = roleRepository.findByName(PredefinedRole.USER_ROLE).orElseThrow(() -> 
            new AppException(ErrorCode.ROLE_NOT_EXISTED));
        if(maintenanceService.isMaintenanceMode() && user.getRoles().contains(role)) {
            throw new AppException(ErrorCode.MAINTENACE_MODE);
        }

        boolean rs = passwordEncoder.matches(aRequest.getPassword(), user.getPassword());
        if(!rs)
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);

        if(user.getIsReset()) {
            return AuthenticationResponse.builder().authenticate(rs).forceChangePassword(user.getIsReset()).build();
        }

        String accessToken = generateToken(user, TokenType.ACCESS);
        String refreshToken = generateToken(user, TokenType.REFRESH);

        return AuthenticationResponse.builder().authenticate(rs).accessToken(accessToken).refreshToken(refreshToken).forceChangePassword(user.getIsReset()).build();
    }

    /**
     * Generates a JWT token for the given user.
     *
     * @param user the user for whom the token is generated.
     * @param verifyEmail flag to determine if the token is for email verification.
     * @return the generated JWT token as a string.
     * @throws AppException if an error occurs during token creation.
     * @implNote Uses a JWT builder to generate and sign tokens with specific claims.
     */
    @Override
    public String generateToken(User user, TokenType tokenType) {
        try {
            // Header
            JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

            long duration;
            switch (tokenType) {
                case REFRESH -> duration = REFRESH_DURATION;
                case VERIFY_MAIL -> duration = MAIL_DURATION;
                default -> duration = VALID_DURATION;
            }

            // JWT claims
            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getEmail())
                    .issuer("NTL")
                    .issueTime(new Date())
                    .expirationTime(new Date(Instant.now().plus(duration, ChronoUnit.SECONDS).toEpochMilli()))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("scope", buildScope(user))
                    .claim("type", tokenType.name())
                    .build();

            // Build Token
            JWSObject jwsObject = new JWSObject(jwsHeader, new Payload(jwtClaimsSet.toJSONObject()));

            // Signature
            jwsObject.sign(new MACSigner(SIGN_KEY.getBytes()));

            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token ", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Logs out a user by invalidating the given token.
     *
     * @param aRequest the token request containing the token to be invalidated.
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
            invalid = false;
            log.error(token, e.getMessage());
            throw new AppException(ErrorCode.JWT_TOKEN_INVALID);
        }

    }
    private void invalidToken(String token) throws ParseException, JOSEException {
        var sigToken = verifyToken(token, false); 

        String jwtID = sigToken.getJWTClaimsSet().getJWTID();
        Date expTime = sigToken.getJWTClaimsSet().getExpirationTime();
        InvalidateToken invalidateToken = InvalidateToken.builder()
                .id(jwtID)
                .expiryTime(expTime)
                .build();
                    
        invalidateTokenRepository.save(invalidateToken); 
    }
    /**
     * Verifies a given JWT token.
     *
     * @param token the token to be verified.
     * @param isRefresh flag indicating whether it is a refresh token.
     * @return a signed JWT if verification is successful.
     * @throws AppException if the token is invalid, expired, or already invalidated.
     * @implNote Uses cryptographic validation to verify JWT integrity.
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
     * Refreshes an expired authentication token.
     *
     * @param refreshRequest the request containing the expired token.
     * @return a new authentication response with a fresh token.
     * @throws AppException if the token is invalid or the user does not exist.
     * @implNote Generates a new token by validating the refresh token and issuing a fresh one.
     */
    @Override
    public SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGN_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);
        // check verify or refresh token
        Date expTime = (isRefresh) ? new Date(signedJWT.getJWTClaimsSet()
                                                .getIssueTime()
                                                .toInstant()
                                                .plus(REFRESH_DURATION, ChronoUnit.SECONDS)
                                                .toEpochMilli())
                                   : signedJWT.getJWTClaimsSet().getExpirationTime();
        if(!expTime.after(new Date())) {
            log.error("Token expired");
            throw new AppException(ErrorCode.JWT_TOKEN_EXPIRED);
        }

        boolean rs = signedJWT.verify(verifier);
        if(!rs) 
        {
            log.error("Token expired");
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        }
        if(invalidateTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
        {
            log.error("Invalid token");
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        }
        
        return signedJWT;
    }

    /**
     * Refreshes the authentication token by verifying the provided refresh token, invalidating the old token,
     * and generating a new access token and refresh token.
     *
     * @param refreshRequest the request containing the refresh token.
     * @return an AuthenticationResponse containing the new access token and refresh token.
     * @throws JOSEException if there is an error during JWT processing.
     * @throws ParseException if the refresh token cannot be parsed.
     * @throws AppException if the user associated with the token does not exist.
     * @implNote This method verifies the refresh token, invalidates the old token by storing it in the database,
     * retrieves the user associated with the token, and generates new authentication tokens.
     */
    @Override
    public AuthenticationResponse refreshToken(TokenRequest refreshRequest) throws JOSEException, ParseException {
        var signedJWT = verifyToken(refreshRequest.getToken(), true);

        String jwtID = signedJWT.getJWTClaimsSet().getJWTID();
        Date expTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidateToken invalidateToken = InvalidateToken.builder()
                .id(jwtID)
                .expiryTime(expTime)
                .build();
        invalidateTokenRepository.save(invalidateToken);

        String email = signedJWT.getJWTClaimsSet().getSubject();

        User user = userRepository.findByEmail(email).orElseThrow(() ->
        new AppException(ErrorCode.USER_NOT_EXISTED));

        String accesstoken = generateToken(user, TokenType.ACCESS);
        String refreshtoken = generateToken(user, TokenType.REFRESH);
        return AuthenticationResponse.builder().authenticate(true).accessToken(accesstoken).refreshToken(refreshtoken).build();
    }

    /**
     * Registers a new user by saving their details, encoding the password, assigning roles,
     * and sending a verification email.
     *
     * @param registerRequest the request containing user registration details.
     * @return the response containing user information.
     * @throws AppException if the user already exists, the role is not found, or an unexpected error occurs.
     * @implNote This method maps the registration request to a User entity, encodes the password,
     * checks for duplicate emails, assigns the default role, saves the user, generates a verification token,
     * and sends a verification email.
     */
    @Override
    public UserResponse registerUser(RegisterRequest registerRequest) {
        User user = userMapper.fromRegisterRequest(registerRequest);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        if(userRepository.existsByEmail(registerRequest.getEmail())) 
        	throw new AppException(ErrorCode.USER_EXISTED);

        Role role = roleRepository.findByName(PredefinedRole.USER_ROLE).orElseThrow(() -> 
            new AppException(ErrorCode.ROLE_NOT_EXISTED));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        try {
            user.setRoles(roles);
            user.setIsVerified(false);
            user.setIsDeleted(false);
            user.setIsReset(false);
            userRepository.save(user);
            String token = generateToken(user, TokenType.VERIFY_MAIL);

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
     * @throws JOSEException If an error occurs while verifying the token.
     * @throws ParseException If an error occurs while parsing the token.
     * @throws AppException If the user does not exist or cannot be authenticated.
     * @implNote This method checks the validity of the token and uses it to verify the user's email.
     */
    @Override
    public boolean verifyEmail(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGN_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);
        // check verify or refresh token
        Date expTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        boolean rs = signedJWT.verify(verifier);
        if(!expTime.after(new Date()) || !rs) {
            String email = signedJWT.getJWTClaimsSet().getSubject();
            User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            userRepository.delete(user);
            return false;
    
        }
        else {
            String email = signedJWT.getJWTClaimsSet().getSubject();
            User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            user.setIsVerified(true);
            userRepository.save(user);
            return true;
        }
    }

    /**
     * Builds the scope (permissions) for the user based on their roles.
     *
     * @param user The user whose scope is to be built.
     * @return A string containing the user's roles in the format "ROLE_<role>".
     * @throws AppException If the user does not exist.
     * @implNote This method constructs the user's scope based on the roles associated with the user.
     */
    @Override
    public String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        
        if(user == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        log.info("Email of user: {}",user.getEmail());
        log.info("Role of user: {}",user.getRoles());
        if(!user.getRoles().isEmpty()) {
            user.getRoles().forEach(role -> stringJoiner.add("ROLE_" + role.getName()));
        }
        return stringJoiner.toString();
    }

    /**
     * Changes the user's password.
     *
     * @param cpRequest The request containing the old password, new password, and confirm password.
     * @return true if the password was successfully changed.
     * @throws AppException If there are errors related to the password (e.g., passwords do not match, old password is incorrect, new password is the same as the old one).
     * @implNote This method allows the user to change their password, ensuring that the old password is correct and the new password is different from the old one.
     */
    @Override
    public boolean changePassword(ChangePasswordRequest cpRequest) {
        var jwtContex = SecurityContextHolder.getContext();
        String email = jwtContex.getAuthentication().getName();
        
        User user = userRepository.findByEmail(email).orElseThrow(() -> 
        new AppException(ErrorCode.USER_NOT_EXISTED));

        if(!cpRequest.getNewPassword().equals(cpRequest.getConfirmPassword())) 
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);

        boolean rs = passwordEncoder.matches(cpRequest.getOldPassword(), user.getPassword());
        if(!rs) 
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if(passwordEncoder.matches(cpRequest.getNewPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_DUPLICATED);
        }
        user.setPassword(passwordEncoder.encode(cpRequest.getNewPassword()));
        userRepository.save(user);
        return true;
    }

    /**
     * Initiates the password reset process by sending an OTP to the user's email.
     *
     * @param email The email of the user requesting the password reset.
     * @throws AppException If the user does not exist or if the email is not verified.
     * @implNote This method sends an OTP (One-Time Password) to the user's email for password reset.
     */
    @Override
    public void forgetPassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> 
        new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!user.getIsVerified()) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
            
        }
        Integer otp = generateOTP(email);
        mailService.sendEmailOTP(otp, user.getEmail(), true, user.getFullName());
    }

    /**
     * Generates a One-Time Password (OTP) for email verification or password reset.
     *
     * @param email The email address to associate the OTP with.
     * @return The generated OTP.
     * @implNote This method generates a 6-digit OTP and saves it in the database with a 5-minute expiration time.
     */
    @Override
    public Integer generateOTP(String email) {
        Random random = new Random();
        Integer otp = random.nextInt(100000,999999);
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(5);

        otpRepository.save(OtpVerification.builder()
                .email(email)
                .otp(otp)
                .expiredAt(expiredAt)
                .build());
        return otp;
    }

    /**
     * Verifies the OTP provided by the user for password reset.
     *
     * @param token The OTP provided by the user.
     * @param email The email address associated with the OTP.
     * @return An authentication response containing the generated JWT token if OTP is valid.
     * @throws AppException If the OTP does not exist, has expired, or any other error occurs.
     * @implNote This method checks the validity of the OTP and generates a new JWT token for authentication.
     */
    @Override
    public String verifyOTP(Integer token, String email) {
        OtpVerification otp = otpRepository.findByOtp(token).orElseThrow(() -> 
        new AppException(ErrorCode.OTP_NOT_EXISTED));

        User user = userRepository.findByEmail(email).orElseThrow(() -> 
        new AppException(ErrorCode.USER_NOT_EXISTED));
        
        if(otp.getExpiredAt().isBefore(LocalDateTime.now())) 
            throw new AppException(ErrorCode.OTP_EXPIRED);
 
        return generateToken(user, TokenType.ACCESS);
    }

    /**
     * Generates a random password of a specified length.
     *
     * @param length The length of the password to be generated.
     * @return The generated password.
     * @implNote This method generates a random password consisting of letters and digits based on the specified length.
     */
    @Override
    public String generatePassword(int length) {
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }

    /**
     * Resets a user's password using a token.
     *
     * @param token the reset token received by the user.
     * @return the newly generated password.
     * @throws JOSEException if there is an error processing the token.
     * @throws ParseException if the token cannot be parsed.
     * @throws AppException if the user does not exist.
     * @implNote Decodes the token, verifies its validity, and generates a new password for the user.
     */
    @Override
    public String resetPassword(String token) throws JOSEException, ParseException {
        try {
            var signedJWT = verifyToken(token, false);
            String email = signedJWT.getJWTClaimsSet().getSubject();

            User user = userRepository.findByEmail(email).orElseThrow(() -> 
            new AppException(ErrorCode.USER_NOT_EXISTED));

            String password = generatePassword(9);
            user.setPassword(passwordEncoder.encode(password));
            user.setIsReset(true);
            userRepository.save(user);
            
            invalidToken(token);
            return password;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Changes the user's password after a password reset request.
     *
     * @param cpRequest the request containing email, new password, and confirmation password.
     * @return {@code true} if the password is successfully changed.
     * @throws AppException if the user does not exist, the new password and confirmation do not match,
     *                      or the new password is the same as the old password.
     * @implNote This method verifies that the user exists, ensures the new password matches the confirmation password,
     * checks that the new password is not the same as the old password, and updates the password securely.
     */
    @Override
    public boolean changePasswordAfterReset(ChangePassAfterResetRequest cpRequest) {
        User user = userRepository.findByEmail(cpRequest.getEmail()).orElseThrow(() -> 
        new AppException(ErrorCode.USER_NOT_EXISTED));

        if(!cpRequest.getNewPassword().equals(cpRequest.getConfirmPassword())) 
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);

        if(passwordEncoder.matches(cpRequest.getNewPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_DUPLICATED);
        }
        try {
            user.setPassword(passwordEncoder.encode(cpRequest.getNewPassword()));
            user.setIsReset(false);
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
     * @throws AppException if the OTP does not exist, is expired, or the user does not exist.
     * @implNote Checks the validity of the OTP and updates the user's email if valid.
     */
    @Override
    public void verifyChangeEmail(VerifyChangeMailRequest changeMailRequest) {
        OtpVerification otp = otpRepository.findByOtp(changeMailRequest.getOtp()).orElseThrow(() -> 
        new AppException(ErrorCode.OTP_NOT_EXISTED));

        User user = userRepository.findByEmail(changeMailRequest.getOldEmail()).orElseThrow(() -> 
        new AppException(ErrorCode.USER_NOT_EXISTED));

        if (otp.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        user.setEmail(changeMailRequest.getNewEmail());
        userRepository.save(user);
    }

    /**
     * Initiates an email change process by sending an OTP to the new email.
     *
     * @param cMailRequest the request containing old and new email addresses.
     * @throws AppException if the user is not authenticated or does not exist.
     * @implNote Validates the old email, generates an OTP, and sends it to the new email address.
     */
    @Override
    public void changeEmail(ChangeMailRequest cMailRequest) {
        var jwtContex = SecurityContextHolder.getContext();
        Authentication authentication = jwtContex.getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String email = jwtContex.getAuthentication().getName();

        if(!email.equals(cMailRequest.getOldEmail())) 
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
            
        User user = userRepository.findByEmail(email).orElseThrow(() -> 
        new AppException(ErrorCode.USER_NOT_EXISTED));

        int otp = generateOTP(cMailRequest.getNewEmail());
        mailService.sendEmailOTP(otp, cMailRequest.getNewEmail(), false, user.getFullName());
        mailService.sendSimpleEmail(cMailRequest.getOldEmail(),"Thông báo tài khoản yêu cầu đổi email",
                "Tài khoản của bạn đã yêu cầu đổi email, nếu không phải bạn vui lòng liên hệ với chúng tôi");
    }
}
