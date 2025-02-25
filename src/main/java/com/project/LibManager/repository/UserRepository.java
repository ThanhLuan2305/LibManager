package com.project.LibManager.repository;

import com.project.LibManager.entity.User;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
	boolean existsByEmail(String email);
	Optional<User> findByEmail(String email);
	Page<User> findAll(Pageable pageable);
}
