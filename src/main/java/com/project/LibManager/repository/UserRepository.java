package com.project.LibManager.repository;

import com.project.LibManager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

public interface UserRepository extends JpaRepository<User, Long> {

}
