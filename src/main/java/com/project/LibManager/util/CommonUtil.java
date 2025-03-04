package com.project.LibManager.util;

import com.project.LibManager.entity.OtpVerification;
import com.project.LibManager.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;

@Component
@Slf4j
@RequiredArgsConstructor
public class CommonUtil {
    private final Random randomOTP = new Random();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final SecureRandom random = new SecureRandom();

    public Integer generateOTP(String email, OtpVerificationRepository otpRepository) {
        Integer otp = randomOTP.nextInt(100000, 999999);
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(5);

        otpRepository.save(OtpVerification.builder()
                .email(email)
                .otp(otp)
                .expiredAt(expiredAt)
                .build());
        return otp;
    }

    public String generatePassword(int length) {
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }
}
