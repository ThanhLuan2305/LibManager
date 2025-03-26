package com.project.libmanager.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.project.libmanager.entity.Book;
import com.project.libmanager.entity.Borrowing;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {
    boolean existsByUserIdAndBookIdAndReturnDateIsNull(Long userId, Long bookId);

    Optional<Borrowing> findByUserIdAndBookIdAndReturnDateIsNull(Long userId, Long bookId);

    Page<Borrowing> findByUserIdAndReturnDateIsNull(Long userId, Pageable pageable);

    Page<Borrowing> findByUserIdAndReturnDateIsNotNull(Long userId, Pageable pageable);


    boolean existsByBookAndReturnDateIsNull(Book book);

    @Query("SELECT COUNT(b) > 0 FROM Borrowing b WHERE b.user.id = :userId AND b.returnDate IS NULL AND b.dueDate < CURRENT_DATE")
    boolean existsOverdueBorrowingsByUser(Long userId);
}
