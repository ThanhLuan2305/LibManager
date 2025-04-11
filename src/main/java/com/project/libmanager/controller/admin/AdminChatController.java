package com.project.libmanager.controller.admin;

import com.project.libmanager.exception.AppException;
import com.project.libmanager.service.IMessageService;
import com.project.libmanager.service.dto.request.PrivateMessageRequest;
import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.PrivateMessageResponse;
import com.project.libmanager.service.dto.response.TopicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * REST controller for managing admin chat-related operations.
 * Provides endpoints for retrieving private messages between the admin and all users.
 */
@RestController
@RequestMapping("admin/chat")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "JWT Authentication")
@Tag(name = "Admin Chat Management", description = "Endpoints for managing chat by admin")
public class AdminChatController {
    private final IMessageService messageService; // Service for private message operations

    /**
     * Retrieves private messages exchanged between two users.
     *
     * @param senderId   the ID of the user who sent the messages
     * @param receiverId the ID of the user who received the messages
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a list of {@link PrivateMessageResponse} objects
     * @implNote Delegates to {@link IMessageService} to fetch messages and wraps them in an {@link ApiResponse}.
     */
    @Operation(summary = "Get private messages between two users",
            description = "Retrieves all private messages exchanged between the specified sender and receiver.")
    @GetMapping("/private/messages")
    public ResponseEntity<ApiResponse<List<PrivateMessageResponse>>> getPrivateMessages(
            @Parameter(name = "senderId", description = "ID of the sender", example = "1") @RequestParam Long senderId,
            @Parameter(name = "receiverId", description = "ID of the receiver", example = "2") @RequestParam Long receiverId
    ) {
        List<PrivateMessageResponse> messages = messageService.getMessagesBetweenUsers(senderId, receiverId);
        ApiResponse<List<PrivateMessageResponse>> response = ApiResponse.<List<PrivateMessageResponse>>builder()
                .message("Messages retrieved successfully")
                .result(messages)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Sends a private message from one user to another.
     *
     * @param privateMessageRequest the request body containing message details
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with the sent {@link PrivateMessageResponse}
     * @throws AppException if the message cannot be sent or saved
     * @implNote Delegates to {@link IMessageService} to send the message and returns it in an {@link ApiResponse}.
     */
    @Operation(summary = "Send private message",
            description = "Sends a private message from one user to another and returns the message details.")
    @PostMapping("/private/send")
    public ResponseEntity<ApiResponse<PrivateMessageResponse>> sendPrivateMessage(
            @Valid @RequestBody PrivateMessageRequest privateMessageRequest
    ) throws IOException {
        PrivateMessageResponse responseMessage = messageService.sendMessagePrivate(privateMessageRequest);
        ApiResponse<PrivateMessageResponse> response = ApiResponse.<PrivateMessageResponse>builder()
                .message("Private message sent successfully")
                .result(responseMessage)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Sends a group message to a topic.
     *
     * @param senderId the ID of the sender
     * @param topic    the topic to send the message to
     * @param content  the message content
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with the sent {@link PrivateMessageResponse}
     * @throws IOException if the message cannot be sent via WebSocket
     */
    @Operation(summary = "Send group message",
            description = "Sends a message to a group topic and returns the message details.")
    @PostMapping("/group/send")
    public ResponseEntity<ApiResponse<PrivateMessageResponse>> sendGroupMessage(
            @RequestParam Long senderId,
            @RequestParam String topic,
            @RequestParam String content
    ) throws IOException {
        PrivateMessageResponse responseMessage = messageService.sendGroupMessage(senderId, topic, content);
        ApiResponse<PrivateMessageResponse> response = ApiResponse.<PrivateMessageResponse>builder()
                .message("Group message sent successfully")
                .result(responseMessage)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all private messages between the admin and all users.
     *
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a map where keys are user IDs and values are lists of {@link PrivateMessageResponse} objects
     * @throws AppException if the authenticated admin user cannot be retrieved or messages cannot be fetched
     * @implNote Delegates to {@link IMessageService} to fetch messages and wraps the result in an {@link ApiResponse}.
     */
    @Operation(summary = "Get all private messages between admin and all users",
            description = "Retrieves a map of user IDs to their private message conversations with the authenticated admin.")
    @GetMapping("/private/all")
    public ResponseEntity<ApiResponse<Object>> getAllMessagesWithUsers() {
        var result = messageService.getMessAdminWithAllUser();
        ApiResponse<Object> response = ApiResponse.builder()
                .message("Fetched all messages between admin and users")
                .result(result)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new group topic for users to subscribe to.
     *
     * @param topicName   the name of the topic
     * @param description the description of the topic (optional)
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a success message
     * @throws AppException if the topic name is invalid or already exists
     */
    @Operation(summary = "Create a new topic",
            description = "Creates a new group topic for users to subscribe to. Only accessible by admins.")
    @PostMapping("/topics/create")
    public ResponseEntity<ApiResponse<Void>> createTopic(
            @Parameter(description = "Name of the topic", example = "General", required = true) @RequestParam String topicName,
            @Parameter(description = "Description of the topic", example = "General discussion group") @RequestParam(required = false) String description
    ) {
        messageService.createTopic(topicName, description);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Topic " + topicName + " created successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all available group topics.
     *
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a list of {@link TopicResponse} objects representing all topics
     * @implNote Delegates to {@link IMessageService} to fetch all topics and wraps them in an {@link ApiResponse}.
     */
    @Operation(summary = "Get all topics",
            description = "Retrieves all available group topics that users can subscribe to, for admin management.")
    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<List<TopicResponse>>> getAllTopics() {
        List<TopicResponse> topics = messageService.getAllTopics();
        ApiResponse<List<TopicResponse>> response = ApiResponse.<List<TopicResponse>>builder()
                .message("Topics retrieved successfully")
                .result(topics)
                .build();
        return ResponseEntity.ok(response);
    }
}