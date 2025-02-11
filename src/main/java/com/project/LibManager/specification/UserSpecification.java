package com.project.LibManager.specification;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.project.LibManager.entity.Role;
import com.project.LibManager.entity.User;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public class UserSpecification {
    public static Specification<User> filterUsers(String fullname, String email, String role, LocalDate fromDate, LocalDate toDate) {
        return (root, query, criteriaBuilder) -> {
            Specification<User> spec = Specification.where(null);
            
            if (fullname != null && !fullname.isEmpty()) {
                spec = spec.and((userRoot, userQuery, cb) -> 
                cb.like(userRoot.get("fullName"), "%" + fullname + "%"));
            }

            if (email != null && !email.isEmpty()) {
                spec = spec.and((userRoot, userQuery, cb) -> 
                cb.like(userRoot.get("email"), "%" + email + "%"));
            }
            
            if (role != null && !role.isEmpty()) {
                spec = spec.and((userRoot, userQuery, cb) -> {
                Join<User, Role> roleJoin = userRoot.join("roles", JoinType.LEFT);
                return cb.equal(roleJoin.get("name"), role);
                });
            }
            
            if (fromDate != null) {
                spec = spec.and((userRoot, userQuery, cb) -> 
                cb.greaterThanOrEqualTo(userRoot.get("birthDate"), fromDate));
            }
            
            if (toDate != null) {
                spec = spec.and((userRoot, userQuery, cb) -> 
                cb.lessThanOrEqualTo(userRoot.get("birthDate"), toDate));
            }
            
            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }
}
