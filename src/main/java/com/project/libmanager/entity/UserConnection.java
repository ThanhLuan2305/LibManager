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
@Document(collection = "user_connections")
public class UserConnection {
    @Id
    private String userId;
    private boolean connected;
    private Long lastConnectedTime;
}
