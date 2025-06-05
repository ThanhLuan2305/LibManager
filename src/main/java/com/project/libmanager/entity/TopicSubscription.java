package com.project.libmanager.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "topic_subscriptions")
public class TopicSubscription {
    @Id
    private String id;
    private String userId;
    private String topic;
}
