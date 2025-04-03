package com.project.libmanager.repository;

import java.util.List;
import java.util.Optional;

import feign.Param;
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

    @Query("SELECT COUNT(b) > 0 FROM Borrowing b WHERE b.user.id = :userId AND b.returnDate IS NULL")
    boolean existsByUserIdAndReturnDateIsNull(Long userId);

    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.returnDate IS NULL")
    long countBorrowByReturnDateIsNull();

    @Query("SELECT COUNT(*) FROM Borrowing")
    long countBorrow();

    @Query(value = """
                SELECT 
                    m.month, 
                    COALESCE(COUNT(b.id), 0) AS totalBorrowings
                FROM (
                    SELECT 1 AS month UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL 
                    SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL 
                    SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL 
                    SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
                ) AS m
                LEFT JOIN borrowings b 
                    ON MONTH(b.borrow_date) = m.month 
                    AND YEAR(b.borrow_date) = :year
                GROUP BY m.month
                ORDER BY m.month
            """, nativeQuery = true)
    List<Object[]> countBorrowingsByMonth(@Param("year") int year);
}
