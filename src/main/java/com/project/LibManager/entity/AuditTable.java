package com.project.LibManager.entity;

import java.time.Instant;

import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class AuditTable {
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = true)
    private Instant updatedAt;

    @Column(nullable = false, updatable = false)
    private String createdBy;

    @Column(nullable = true)
    private String updatedBy;

    @PrePersist
    public void handleBeforeCreate() {
        var jwtContext = SecurityContextHolder.getContext();

        this.createdBy = jwtContext != null ? jwtContext.getAuthentication().getName() : "";
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        var jwtContext = SecurityContextHolder.getContext();

        this.updatedBy = jwtContext != null ? jwtContext.getAuthentication().getName() : "";
        this.updatedAt = Instant.now();
    }
}
