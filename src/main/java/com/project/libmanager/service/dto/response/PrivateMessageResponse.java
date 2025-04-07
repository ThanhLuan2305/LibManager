package com.project.libmanager.service.dto.response;

import com.project.libmanager.constant.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrivateMessageResponse {
    private String id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Instant timestamp;
    private MessageType messageType;
}
