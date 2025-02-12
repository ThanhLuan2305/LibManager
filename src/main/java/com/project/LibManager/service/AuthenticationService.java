package com.project.LibManager.service;

import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Random;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.project.LibManager.dto.request.AuthenticationRequest;
import com.project.LibManager.dto.request.ChangeMailRequest;
import com.project.LibManager.dto.request.ChangePasswordRequest;
import com.project.LibManager.dto.request.TokenRequest;
import com.project.LibManager.dto.request.UserCreateRequest;
import com.project.LibManager.dto.request.VerifyChangeMailRequest;
import com.project.LibManager.dto.response.AuthenticationResponse;
import com.project.LibManager.dto.response.IntrospectResponse;
import com.project.LibManager.dto.response.UserResponse;
import com.project.LibManager.entity.InvalidateToken;
import com.project.LibManager.entity.OtpVerification;
import com.project.LibManager.entity.User;
import com.project.LibManager.exception.AppException;
import com.project.LibManager.exception.ErrorCode;
import com.project.LibManager.repository.InvalidateTokenRepository;
import com.project.LibManager.repository.OtpVerificationRepository;
import com.project.LibManager.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthenticationService {
    UserRepository userRepository;
    UserService userService;
    InvalidateTokenRepository invalidateTokenRepository;
    MailService mailService;
    OtpVerificationRepository otpRepository;
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.signing.key}")
    protected String SIGN_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected Long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refresh-duration}")
    protected Long REFRESH_DURATION;

    @NonFinal
    @Value("${jwt.mail-duration}")
    protected Long MAIL_DURATION; 

    
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final SecureRandom random = new SecureRandom();

    public AuthenticationResponse authenticate(AuthenticationRequest aRequest) {
        User user = userRepository.findByEmail(aRequest.getEmail());
        if(user == null) 
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        if(!user.getIsVerified()) 
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        boolean rs = passwordEncoder.matches(aRequest.getPassword(), user.getPassword());
        if(!rs)
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        String token = generateToken(user, false);
        return AuthenticationResponse.builder().authenticate(rs).token(token).build();
    } 

    private String generateToken(User user, boolean verifyEmail) {
        //Header
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet =(verifyEmail) ? new JWTClaimsSet.Builder()
                                                            .subject(user.getEmail()).issuer("NTL")
                                                            .issueTime(new Date())
                                                            .expirationTime(new Date(
                                                                Instant.now().plus(MAIL_DURATION,ChronoUnit.SECONDS).toEpochMilli()
                                                            ))
                                                            .jwtID(UUID.randomUUID().toString())
                                                            .claim("scope", buildScope(user))
                                                            .build()
                                                : new JWTClaimsSet.Builder()
                                                            .subject(user.getEmail()).issuer("NTL")
                                                            .issueTime(new Date())
                                                            .expirationTime(new Date(
                                                                Instant.now().plus(VALID_DURATION,ChronoUnit.SECONDS).toEpochMilli()
                                                            ))
                                                            .jwtID(UUID.randomUUID().toString())
                                                            .claim("scope", buildScope(user))
                                                            .build();
        //Payload
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        //Build Token
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        //Signature
        try {
            jwsObject.sign(new MACSigner(SIGN_KEY.getBytes()));
        } catch (JOSEException e) {
            log.error("Cannot create token ", e.getMessage());
        }
        return jwsObject.serialize();
    }
    
    // Verify token
    public IntrospectResponse introspectToken(TokenRequest iRequest) throws JOSEException, ParseException {
        String token = iRequest.getToken();
        boolean invalid = true;

        try {
            verifyToken(token, false);
        } catch (Exception e) {
            invalid = false;
        }

        return IntrospectResponse.builder().valid(invalid).build(); 
    }

    // Logout user
    public void logout(TokenRequest aRequest) throws Exception, ParseException {
        try {
            var signToken = verifyToken(aRequest.getToken(), false); 

            String jwtID = signToken.getJWTClaimsSet().getJWTID();
            Date expTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidateToken invalidateToken = InvalidateToken.builder()
                    .id(jwtID)
                    .expiryTime(expTime)
                    .build();

            invalidateTokenRepository.save(invalidateToken); 
        } catch (AppException e) {
            log.info("Token already expired");
        }   
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGN_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);
        // check verify or refresh token
        Date expTime = (isRefresh) ? new Date(signedJWT.getJWTClaimsSet()
                                                .getIssueTime()
                                                .toInstant()
                                                .plus(REFRESH_DURATION, ChronoUnit.SECONDS)
                                                .toEpochMilli())
                                   : signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean rs = signedJWT.verify(verifier);

        if(!(rs && expTime.after(new Date()))) 
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if(invalidateTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        
        return signedJWT;
    }

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

        User user = userRepository.findByEmail(email);
        if(user == null) 
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        String token = generateToken(user, false);
        return AuthenticationResponse.builder().authenticate(true).token(token).build();
    }

    public UserResponse registerUser(UserCreateRequest userCreateRequest) {
        var createdUser = userService.createUser(userCreateRequest);
        User user = userRepository.findByEmail(createdUser.getEmail());
        String token = generateToken(user, true);

        // send email verify
        mailService.sendEmailVerify(userCreateRequest.getFullName(), token, userCreateRequest.getEmail());
        return createdUser;
    }

    public boolean verifyEmail(String token) throws JOSEException, ParseException {
        boolean rs = introspectToken(new TokenRequest().builder().token(token).build()).isValid();
        if(rs) {
            var signedJWT = verifyToken(token, false);
            String email = signedJWT.getJWTClaimsSet().getSubject();
            User user = userRepository.findByEmail(email);
            if(user == null) 
                throw new AppException(ErrorCode.USER_NOT_EXISTED);
            user.setIsVerified(true);
            userRepository.save(user);
        }
        else throw new AppException(ErrorCode.UNAUTHENTICATED);
        return true;
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if(!user.getRoles().isEmpty()) {
            user.getRoles().forEach(role -> stringJoiner.add("ROLE_" + role.getName()));
        }
        return stringJoiner.toString();
    }

    public boolean changePassword(ChangePasswordRequest cpRequest) {
        var jwtContex = SecurityContextHolder.getContext();
        String email = jwtContex.getAuthentication().getName();
        
        User user = userRepository.findByEmail(email);
        if(user == null) 
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
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
    public void forgetPassword(String email) {
        User user = userRepository.findByEmail(email);
        if(user == null) 
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        if (!user.getIsVerified()) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
            
        }
        Integer otp = generateOTP(email);
        mailService.sendEmailOTP(otp, user.getEmail(), true, user.getFullName());
    }
    private Integer generateOTP(String email) {
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
    public AuthenticationResponse verifyOTP(Integer token, String email) {
        OtpVerification otp = otpRepository.findByOtp(token);
        User user = userRepository.findByEmail(email);
        String tokenJWT = "";
        if(otp == null) 
            throw new AppException(ErrorCode.OTP_NOT_EXISTED);
        if(otp.getExpiredAt().isBefore(LocalDateTime.now())) 
            throw new AppException(ErrorCode.OTP_EXPIRED);
        tokenJWT = generateToken(user, false);
        return AuthenticationResponse.builder().authenticate(true).token(tokenJWT).build();
    }
    public String resetPassword(String token) throws Exception {
        var signedJWT = verifyToken(token, false);
        String email = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findByEmail(email);
        if(user == null) 
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        String password = generatePassword(9);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        logout(TokenRequest.builder().token(token).build());
        return password;
    }

    public static String generatePassword(int length) {
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }

    public void verifyChangeEmail(VerifyChangeMailRequest changeMailRequest) {
        OtpVerification otp = otpRepository.findByOtp(changeMailRequest.getOtp());
        if (otp == null) {
            throw new AppException(ErrorCode.OTP_NOT_EXISTED);
        }

        User user = userRepository.findByEmail(changeMailRequest.getOldEmail());
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        if (otp.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        user.setEmail(changeMailRequest.getNewEmail());
        userRepository.save(user);
    }
    
    public void changeEmail(ChangeMailRequest cMailRequest) {
       var jwtContex = SecurityContextHolder.getContext();
        String email = jwtContex.getAuthentication().getName();

        if(!email.equals(cMailRequest.getOldEmail())) 
            throw new AppException(ErrorCode.UNAUTHENTICATED);
            
        User user = userRepository.findByEmail(email);  
        if(user == null) 
            throw new AppException(ErrorCode.USER_NOT_EXISTED);

        int otp = generateOTP(cMailRequest.getNewEmail());
        mailService.sendEmailOTP(otp, cMailRequest.getNewEmail(), false, user.getFullName());
        mailService.sendSimpleEmail(cMailRequest.getOldEmail(),"Thông báo tài khoản yêu cầu đổi email",
                "Tài khoản của bạn đã yêu cầu đổi email, nếu không phải bạn vui lòng liên hệ với chúng tôi");
    }
}
