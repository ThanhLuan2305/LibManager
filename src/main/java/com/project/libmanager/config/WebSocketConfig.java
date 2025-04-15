package com.project.libmanager.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

// This class sets up WebSocket for the application
@Configuration
@EnableWebSocket // Turns on WebSocket support
@RequiredArgsConstructor // Creates constructor for final fields
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketChatHandler chatHandler; // Handles chat messages

    // This method sets up the WebSocket connection
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler, "/chat") // Connects chatHandler to "/chat" URL
                .addInterceptors(new HttpSessionHandshakeInterceptor() {
                    // This method runs before WebSocket connection starts
                    @Override
                    public boolean beforeHandshake(org.springframework.http.server.ServerHttpRequest request,
                                                   org.springframework.http.server.ServerHttpResponse response,
                                                   org.springframework.web.socket.WebSocketHandler wsHandler,
                                                   Map<String, Object> attributes) {
                        // Get userId from URL query
                        String userId = extractUserIdFromQuery(request.getURI().getQuery());
                        if (userId != null) {
                            // If userId is valid, save it for the connection
                            attributes.put("userId", userId);
                            return true; // Allow connection
                        }
                        return false; // Block connection if userId is invalid
                    }

                    // This method gets userId from query string (like "?userId=123")
                    private String extractUserIdFromQuery(String query) {
                        // Check if query is empty or null
                        if (query == null || query.isEmpty()) {
                            return null; // No userId found
                        }
                        // Split query into parts (e.g., "userId=123&other=abc")
                        for (String param : query.split("&")) {
                            // Look for "userId=" in query
                            if (param.startsWith("userId=")) {
                                // Get the value after "userId="
                                String userId = param.substring("userId=".length());
                                // Check if userId is a number
                                return isNumeric(userId) ? userId : null;
                            }
                        }
                        return null; // No valid userId found
                    }

                    // This method checks if a string is a number
                    private boolean isNumeric(String str) {
                        // Return true if str is not null, not empty, and contains only digits
                        return str != null && !str.isEmpty() && str.matches("\\d+");
                    }
                })
                .setAllowedOrigins("http://localhost:5173"); // Allow requests from this URL
    }
}