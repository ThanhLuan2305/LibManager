package com.project.libmanager.repository;

import com.project.libmanager.entity.Topic;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TopicRepository extends MongoRepository<Topic, String> {
    boolean existsByName(String name);
}
