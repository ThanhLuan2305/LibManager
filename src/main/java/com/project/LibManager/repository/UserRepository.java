package com.project.LibManager.repository;

import com.project.LibManager.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
	boolean existsByEmail(String email);
	User findByEmail(String email);
	Page<User> findAll(Pageable pageable);
	Page<User> findAll(Specification<User> spec, Pageable pageable);
}
