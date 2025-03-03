package com.project.LibManager.entity;

import java.time.LocalDate;
import java.util.Set;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "books")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book extends AuditTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String isbn;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private BookType type;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private String publisher;

    @Column(nullable = false)
    private LocalDate publishedDate;

    @Column(nullable = false)
    private int maxBorrowDays;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String coverImageUrl;

    @Column(nullable = false)
    private Boolean isDeleted;

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    private Set<Borrowing> borrowings;
}
