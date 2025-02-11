package com.project.LibManager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.LibManager.entity.Borrowing;

public interface BorrowingRepository extends JpaRepository<Borrowing,Long>{
    
}
