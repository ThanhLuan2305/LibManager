package com.project.libmanager.repository;

import com.project.libmanager.entity.UserConnection;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserConnectionRepository extends MongoRepository<UserConnection, String> {
    boolean existsByUserIdAndConnectedTrue(String userId);
}
