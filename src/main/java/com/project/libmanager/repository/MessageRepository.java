package com.project.libmanager.repository;

import com.project.libmanager.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    @Query("{ '$or': [ " +
            "  { 'senderId': ?0, 'receiverId': ?1 }, " +
            "  { 'senderId': ?1, 'receiverId': ?0 } " +
            "] }")
    List<Message> findMessagesBetweenUsers(Long senderId, Long receiverId);

    List<Message> findByReceiverIdAndDeliveredFalse(Long receiverId);

    List<Message> findByTopicAndDeliveredFalse(String topic);

    List<Message> findByTopic(String topic);
}
