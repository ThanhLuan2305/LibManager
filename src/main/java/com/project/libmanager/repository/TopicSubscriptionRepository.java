package com.project.libmanager.repository;

import com.project.libmanager.entity.TopicSubscription;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TopicSubscriptionRepository extends MongoRepository<TopicSubscription, String> {
    List<TopicSubscription> findByTopic(String topic);

    boolean existsByUserIdAndTopic(String userId, String topic);

    void deleteByUserIdAndTopic(String userId, String topic);

    List<TopicSubscription> findByUserId(String userId);
}
