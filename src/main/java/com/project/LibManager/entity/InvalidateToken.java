package com.project.LibManager.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invalidate_token")
@EqualsAndHashCode(callSuper = false)
public class InvalidateToken extends AuditTable {
    @Id
    private String id;

    @Column
    private Instant expiryTime;
}
