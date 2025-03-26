package com.project.libmanager.entity;

import com.project.libmanager.constant.UserAction;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "activity_logs")
@Data
public class ActivityLog {
    @Id
    private String id;
    private Long userId;
    private String email;
    private UserAction action;
    private String details;
    private Instant timestamp = Instant.now();
}
