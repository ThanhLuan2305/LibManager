package com.project.LibManager.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.LibManager.entity.Book;

public interface BookRepository extends JpaRepository<Book,Long> {
    Page<Book> findAll(Pageable pageable);
    Page<Book> findAll(Specification<Book> specification, Pageable pageable);
    Optional<Book> findByIsbn(String isbn);
} 
