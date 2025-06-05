package com.project.libmanager.repository;

import com.project.libmanager.entity.LoginDetail;
import com.project.libmanager.entity.User;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LoginDetailRepository extends JpaRepository<LoginDetail, Long> {
    boolean existsByJti(String  jti);

    Optional<LoginDetail> findByJti(String jti);

    @Query("SELECT l FROM LoginDetail l WHERE l.jti = :jti AND l.enabled = true")
    Optional<LoginDetail> findByJtiAndEnabled(@Param("jti") String jti);

    List<LoginDetail> findByUserId(Long userId);
    void deleteByUser(User user);
}