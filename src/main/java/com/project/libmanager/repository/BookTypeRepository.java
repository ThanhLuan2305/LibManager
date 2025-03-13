package com.project.libmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.libmanager.entity.BookType;

public interface BookTypeRepository extends JpaRepository<BookType, Long> {

}
