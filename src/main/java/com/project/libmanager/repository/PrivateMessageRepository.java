package com.project.libmanager.repository;

import com.project.libmanager.entity.PrivateMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PrivateMessageRepository extends MongoRepository<PrivateMessage, String> {

    @Query("{ '$or': [ " +
            "  { 'senderId': ?0, 'receiverId': ?1 }, " +
            "  { 'senderId': ?1, 'receiverId': ?0 } " +
            "] }")
    List<PrivateMessage> findMessagesBetweenUsers(Long userId1, Long userId2);

    void deleteById(String id);
}
