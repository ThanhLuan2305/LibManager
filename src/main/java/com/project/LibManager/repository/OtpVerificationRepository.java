package com.project.LibManager.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.LibManager.entity.OtpVerification;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    boolean deleteByExpiredAtBefore(LocalDateTime expiredAt);
    OtpVerification findByEmail(String email);
    OtpVerification findByOtp(Integer otp);
}
