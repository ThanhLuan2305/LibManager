package com.project.libmanager.util;

import com.project.libmanager.repository.OtpVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class CommonUtil {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final SecureRandom random = new SecureRandom();
    private final OtpVerificationRepository otpRepository;

    public String generateOTP() {
        int otp = random.nextInt(1000000);
        return String.format("%06d", otp);
    }

    public String generatePassword(int length) {
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }

    public String generateJTI() {
        return UUID.randomUUID().toString();
    }
}
