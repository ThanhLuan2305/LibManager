package com.project.libmanager.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class CommonUtil {
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*";
    private static final String ALL_CHARACTERS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARACTERS;
    private static final SecureRandom random = new SecureRandom();

    public String generateOTP() {
        int otp = random.nextInt(1000000);
        return String.format("%06d", otp);
    }

    public static String generatePassword(int length) {
        if (length < 7) {
            throw new IllegalArgumentException("Password length must be at least 7");
        }

        List<Character> passwordChars = new ArrayList<>();
        passwordChars.add(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        passwordChars.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
        passwordChars.add(SPECIAL_CHARACTERS.charAt(random.nextInt(SPECIAL_CHARACTERS.length())));

        for (int i = 3; i < length; i++) {
            passwordChars.add(ALL_CHARACTERS.charAt(random.nextInt(ALL_CHARACTERS.length())));
        }

        Collections.shuffle(passwordChars);

        StringBuilder password = new StringBuilder();
        for (char ch : passwordChars) {
            password.append(ch);
        }

        return password.toString();
    }

    public String generateJTI() {
        return UUID.randomUUID().toString();
    }
}
