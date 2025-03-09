package com.project.libmanager.repository;

import com.project.libmanager.entity.User;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
	boolean existsByEmail(String email);

	Optional<User> findByEmail(String email);

	@NonNull
	Page<User> findAll(@NonNull Pageable pageable);
}
