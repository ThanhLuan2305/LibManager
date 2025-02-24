package com.project.LibManager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.project.LibManager.entity.Book;
import com.project.LibManager.entity.Borrowing;

public interface BorrowingRepository extends JpaRepository<Borrowing,Long>{
    boolean existsByUserIdAndBookIdAndReturnDateIsNull(Long userId, Long bookId);

    Optional<Borrowing> findByUserIdAndBookIdAndReturnDateIsNull(Long userId, Long bookId);
    List<Borrowing> findByUserIdAndReturnDateIsNull(Long bookId);

    boolean existsByBookAndReturnDateIsNull(Book book);

    @Query("SELECT COUNT(b) > 0 FROM Borrowing b WHERE b.user.id = :userId AND b.returnDate IS NULL AND b.dueDate < CURRENT_DATE")
    boolean existsOverdueBorrowingsByUser(Long userId);
}   
