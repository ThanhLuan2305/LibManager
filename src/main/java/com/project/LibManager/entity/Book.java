package com.project.LibManager.entity;

import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "books")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String isbn;

    @Column(nullable = false)
    String title;

    @Column(nullable = false)
    String author;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    BookType type;

    @Column(nullable = false)
    int stock;  

    @Column(nullable = false)
    String publisher;

    @Column(nullable = false)
    LocalDate publishedDate;

    @Column(nullable = false)
    int maxBorrowDays;

    @Column(nullable = false)
    String location;

    @Column(nullable = false)
    String coverImageUrl;

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    Set<Borrowing> borrowings;
}

