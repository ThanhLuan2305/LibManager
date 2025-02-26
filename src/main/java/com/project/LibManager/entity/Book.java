package com.project.LibManager.entity;

import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {
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
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private Set<Borrowing> borrowings;
}

