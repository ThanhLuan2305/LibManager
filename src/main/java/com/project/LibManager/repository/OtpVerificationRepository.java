package com.project.LibManager.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.LibManager.entity.OtpVerification;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    boolean deleteByExpiredAtBefore(LocalDateTime expiredAt);

    Optional<OtpVerification> findByOtp(Integer otp);
}
