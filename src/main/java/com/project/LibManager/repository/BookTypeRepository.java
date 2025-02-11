package com.project.LibManager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.LibManager.entity.BookType;

public interface BookTypeRepository extends JpaRepository<BookType, Long> {

}
