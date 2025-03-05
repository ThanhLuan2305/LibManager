package com.project.LibManager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "borrowings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Borrowing extends AuditTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private Book book;

    @Column(nullable = false)
    private Instant borrowDate;

    @Column(nullable = false)
    private Instant dueDate;

    @Column
    private Instant returnDate;
}
