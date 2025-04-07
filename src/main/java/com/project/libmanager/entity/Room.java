package com.project.libmanager.entity;

import lombok.Builder;
import lombok.Data;
import jakarta.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "room")
@Builder
@Data
public class Room {
    @Id
    private String id;
    private String roomId;
    private String name;
    private List<Message> messages;
    private List<Long> userIds;
}
