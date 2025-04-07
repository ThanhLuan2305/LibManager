package com.project.libmanager.service.dto.request;

import com.project.libmanager.constant.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrivateMessageRequest {
    private Long senderId;
    private Long receiverId;
    private String content;
    private MessageType messageType;
}
