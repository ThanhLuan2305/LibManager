package com.project.libmanager.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@Document(collection = "messages")
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    private String id;
    private Long senderId;
    private Long receiverId;
    private String topic;
    private String content;
    private Instant timestamp;
    private boolean delivered;
    private boolean read;
}