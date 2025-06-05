package com.project.libmanager.repository;

import java.util.List;
import java.util.Optional;

import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import com.project.libmanager.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    @NonNull
    Page<Book> findAll(@NonNull Pageable pageable);

    Optional<Book> findByIsbn(String isbn);

    @Query("SELECT b FROM Book b WHERE b.deleted = false")
    Page<Book> findAllAvailableBooks(@NonNull Pageable pageable);

    @Query("SELECT SUM(b.stock) FROM Book b WHERE b.deleted = false")
    long countBookActive();

    @Query(value = "SELECT * FROM books WHERE deleted = false ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<Book> findRecentBooks(@Param("limit") int limit);

}
