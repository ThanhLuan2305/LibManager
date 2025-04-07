package com.project.libmanager.entity;

import com.project.libmanager.constant.MessageType;
import lombok.Builder;
import lombok.Data;
import jakarta.persistence.Id;

import java.time.Instant;

@Builder
@Data
public class Message {
    @Id
    private String messageId;

    private Long sender;

    private String content;

    private Instant timeStamp;

    private MessageType messageType;
}
