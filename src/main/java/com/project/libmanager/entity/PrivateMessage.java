package com.project.libmanager.entity;

import com.project.libmanager.constant.MessageType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@Document(collection = "private_messages")
public class PrivateMessage {
    @Id
    private String id;

    private Long senderId;
    private Long receiverId;

    private String content;
    private Instant timestamp;
    private MessageType messageType;
}
