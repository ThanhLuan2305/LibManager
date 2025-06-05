package com.project.libmanager.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Utility class providing common helper methods for generating random identifiers.
 * Includes generation of OTPs and JTIs for authentication and verification purposes.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CommonUtil {
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";           // Uppercase letters for potential use
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";           // Lowercase letters for potential use
    private static final String DIGITS = "0123456789";                             // Digits used in OTP generation
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*";                   // Special characters for potential use
    private static final String ALL_CHARACTERS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARACTERS; // Combined character set
    private static final SecureRandom random = new SecureRandom();                 // Cryptographically secure random generator

    /**
     * Generates a 6-digit one-time password (OTP).
     *
     * @return a string representing a 6-digit OTP (e.g., "123456")
     * @implNote Uses SecureRandom to generate a random integer between 0 and 999999,
     * padded with leading zeros to ensure 6 digits.
     */
    public String generateOTP() {
        // Generate random number; range 0-999999
        int otp = random.nextInt(1000000);
        // Format to 6 digits; pads with leading zeros (e.g., 123 -> "000123")
        return String.format("%06d", otp);
    }

    /**
     * Generates a unique JWT Identifier (JTI) using UUID.
     *
     * @return a string representing a UUID (e.g., "550e8400-e29b-41d4-a716-446655440000")
     * @implNote Uses UUID.randomUUID() to ensure uniqueness and randomness.
     */
    public String generateJTI() {
        // Generate UUID; cryptographically strong random identifier
        return UUID.randomUUID().toString();
    }
}