package com.project.LibManager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.LibManager.entity.Borrowing;

public interface BorrowingRepository extends JpaRepository<Borrowing,Long>{
    boolean existsByUserIdAndBookIdAndReturnDateIsNull(Long userId, Long bookId);

    Optional<Borrowing> findByUserIdAndBookIdAndReturnDateIsNull(Long userId, Long bookId);
    List<Borrowing> findByUserIdAndReturnDateIsNull(Long bookId);
}   
