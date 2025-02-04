package com.project.LibManager.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "books")
@Data
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private BookType type;

    @Column(nullable = false)
    private int stock;
}

