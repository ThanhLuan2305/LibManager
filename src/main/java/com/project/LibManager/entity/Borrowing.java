package com.project.LibManager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
    private LocalDate borrowDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column
    private LocalDate returnDate;
}
