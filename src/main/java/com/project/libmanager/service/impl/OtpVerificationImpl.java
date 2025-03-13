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

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpVerificationImpl implements IOtpVerificationService {

    private final OtpVerificationRepository otpRepository;

    @Transactional
    @Override
    public void createOtp(OtpVerification otpVerification, boolean isPhone) {

        String phoneOrEmail = isPhone ? otpVerification.getPhoneNumber() : otpVerification.getEmail();

        if (otpRepository.existsByPhoneOrEmailAndType(phoneOrEmail, otpVerification.getType(), isPhone)) {
            throw new AppException(ErrorCode.OTP_IS_DULICATED);
        }

        otpRepository.save(otpVerification);
    }

    @Transactional
    @Override
    public void deleteOtp(String contactInfo, OtpType type, boolean isPhone) {
        OtpVerification otpVerification;
        if(isPhone){
            otpVerification = otpRepository.findByPhoneNumberAndType(contactInfo, type).orElseThrow(() -> new AppException(ErrorCode.OTP_NOT_EXISTED));
        } else {
            otpVerification = otpRepository.findByEmailAndType(contactInfo, type).orElseThrow(() -> new AppException(ErrorCode.OTP_NOT_EXISTED));
        }
        otpRepository.delete(otpVerification);
    }

    @Override
    public boolean verifyOtp(String otp, String contactInfo, OtpType type, boolean isPhone) {
        OtpVerification otpVerification = (isPhone
                ? otpRepository.findByPhoneNumberAndType(contactInfo, type)
                : otpRepository.findByEmailAndType(contactInfo, type))
                .orElseThrow(() -> {
                    log.warn("OTP not found for contact: {}, type: {}", contactInfo, type);
                    return new AppException(ErrorCode.OTP_NOT_EXISTED);
                });

        if (!otp.equals(otpVerification.getOtp())) {
            log.warn("OTP not match");
            throw new AppException(ErrorCode.OTP_INVALID);
        }

        if (otpVerification.getExpiredAt().isBefore(Instant.now())) {
            log.warn("OTP expired for contact: {}, type: {}", contactInfo, type);
            deleteOtp(contactInfo, type, isPhone);
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        deleteOtp(contactInfo, type, isPhone);
        return true;
    }
}
