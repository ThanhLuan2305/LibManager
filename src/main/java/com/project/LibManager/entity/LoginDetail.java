package com.project.LibManager.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = false)
@Table(name = "login_detail")
public class LoginDetail extends AuditTable{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 512)
    private String accessToken;

    @Column(nullable = false, unique = true, length = 512)
    private String refreshToken;

    @Column(nullable = false)
    private boolean status;

    @Column(nullable = false)
    private Instant expiredAt;

    @Column(nullable = false)
    private Instant refreshTokenExpiredAt;

    @Column(nullable = false, length = 36)
    private String userId;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;
}
