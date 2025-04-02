package com.project.libmanager.service.impl;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.constant.OtpType;
import com.project.libmanager.entity.OtpVerification;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.repository.OtpVerificationRepository;
import com.project.libmanager.service.IOtpVerificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Implementation of {@link IOtpVerificationService} for managing OTP (One-Time Password) operations.
 * Provides creation, deletion, and verification of OTPs for phone or email verification.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OtpVerificationImpl implements IOtpVerificationService {

    private final OtpVerificationRepository otpRepository; // Repository for OTP persistence and queries

    /**
     * Creates a new OTP record for phone or email verification.
     *
     * @param otpVerification the {@link OtpVerification} entity containing:
     *                        - phoneNumber: phone number (if isPhone is true)
     *                        - email: email address (if isPhone is false)
     *                        - type: OTP type (e.g., REGISTRATION, RESET_PASSWORD)
     *                        - otp: generated OTP code
     *                        - expiredAt: expiration timestamp
     * @param isPhone         true if OTP is for phone number, false if for email
     * @throws AppException if an OTP already exists for the contact info and type (ErrorCode.OTP_IS_DULICATED)
     * @implNote Ensures uniqueness of OTP per contact info and type; saves OTP transactionally.
     */
    @Transactional
    @Override
    public void createOtp(OtpVerification otpVerification, boolean isPhone) {
        // Determine contact info based on isPhone flag; assumes entity fields are populated
        String phoneOrEmail = isPhone ? otpVerification.getPhoneNumber() : otpVerification.getEmail();

        // Check for existing OTP to prevent duplicates; enforces one active OTP per type
        if (otpRepository.existsByPhoneOrEmailAndType(phoneOrEmail, otpVerification.getType(), isPhone)) {
            throw new AppException(ErrorCode.OTP_IS_DULICATED); // Fail fast if OTP already exists
        }

        // Persist OTP entity; assumes transactional context handles rollback on failure
        otpRepository.save(otpVerification);
    }

    /**
     * Deletes an OTP record by contact info and type.
     *
     * @param contactInfo the phone number or email address associated with the OTP
     * @param type        the {@link OtpType} of the OTP (e.g., REGISTRATION, RESET_PASSWORD)
     * @param isPhone     true if contactInfo is a phone number, false if an email
     * @throws AppException if no OTP exists for the contact info and type (ErrorCode.OTP_NOT_EXISTED)
     * @implNote Retrieves and deletes OTP transactionally; assumes contact info is unique per type.
     */
    @Transactional
    @Override
    public void deleteOtp(String contactInfo, OtpType type, boolean isPhone) {
        // Declare OTP entity variable for retrieval
        OtpVerification otpVerification;
        // Fetch OTP based on contact type; uses different queries for phone vs email
        if (isPhone) {
            otpVerification = otpRepository.findByPhoneNumberAndType(contactInfo, type)
                    .orElseThrow(() -> new AppException(ErrorCode.OTP_NOT_EXISTED)); // Fail if not found
        } else {
            otpVerification = otpRepository.findByEmailAndType(contactInfo, type)
                    .orElseThrow(() -> new AppException(ErrorCode.OTP_NOT_EXISTED)); // Fail if not found
        }
        // Delete OTP from repository; assumes transactional context ensures consistency
        otpRepository.delete(otpVerification);
    }

    /**
     * Verifies an OTP against stored data and deletes it upon success or expiration.
     *
     * @param otp         the OTP code provided by the user
     * @param contactInfo the phone number or email address associated with the OTP
     * @param type        the {@link OtpType} of the OTP (e.g., REGISTRATION, RESET_PASSWORD)
     * @param isPhone     true if contactInfo is a phone number, false if an email
     * @return true if OTP is valid and verified successfully
     * @throws AppException if:
     *                      - OTP not found (ErrorCode.OTP_NOT_EXISTED)
     *                      - OTP code is incorrect (ErrorCode.OTP_INVALID)
     *                      - OTP has expired (ErrorCode.OTP_EXPIRED)
     * @implNote Validates OTP code and expiration, deletes OTP after verification or if expired.
     */
    @Override
    public boolean verifyOtp(String otp, String contactInfo, OtpType type, boolean isPhone) {
        // Fetch OTP using ternary operator; concise but assumes contact info is valid
        OtpVerification otpVerification = (isPhone
                ? otpRepository.findByPhoneNumberAndType(contactInfo, type)
                : otpRepository.findByEmailAndType(contactInfo, type))
                .orElseThrow(() -> {
                    // Log warning for debugging; indicates OTP not generated or already deleted
                    log.warn("OTP not found for contact: {}, type: {}", contactInfo, type);
                    return new AppException(ErrorCode.OTP_NOT_EXISTED); // Fail if OTP missing
                });

        // Compare provided OTP with stored value; assumes case-sensitive match
        if (!otp.equals(otpVerification.getOtp())) {
            log.warn("OTP not match");
            throw new AppException(ErrorCode.OTP_INVALID); // Fail if OTP incorrect
        }

        // Check expiration against current time; uses Instant for precision
        if (otpVerification.getExpiredAt().isBefore(Instant.now())) {
            log.warn("OTP expired for contact: {}, type: {}", contactInfo, type);
            // Clean up expired OTP; ensures no stale data remains
            deleteOtp(contactInfo, type, isPhone);
            throw new AppException(ErrorCode.OTP_EXPIRED); // Fail if OTP expired
        }

        // OTP is valid; delete it to prevent reuse
        deleteOtp(contactInfo, type, isPhone);
        // Return success; indicates verification completed
        return true;
    }
}