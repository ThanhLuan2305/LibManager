package com.project.LibManager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.LibManager.entity.InvalidateToken;

public interface InvalidateTokenRepository extends JpaRepository<InvalidateToken, String> {

}
