package com.project.libmanager.controller.user;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * REST controller for managing chat-related operations.
 * Provides endpoints for sending, retrieving, and deleting private messages.
 */
@Tag(name = "Chat Management", description = "Endpoints for managing private messages between users")
@RestController
@RequestMapping("user/chat")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
public class ChatController {
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
     * Subscribes a user to a topic to receive group messages.
     *
     * @param topic the topic to subscribe to
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a success message
     */
    @Operation(summary = "Subscribe to a topic",
            description = "Subscribes a user to a group topic to receive messages.")
    @PostMapping("/topics/subscribe")
    public ResponseEntity<ApiResponse<Void>> subscribeToTopic(
            @Parameter(name = "topic", description = "The topic to subscribe to", example = "general") @RequestParam String topic
    ) {
        messageService.subscribeToTopic(topic);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Subscribed to topic " + topic + " successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Unsubscribes a user from a topic to stop receiving group messages.
     *
     * @param topic the topic to unsubscribe from
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a success message
     */
    @Operation(summary = "Unsubscribe from a topic",
            description = "Unsubscribes a user from a group topic.")
    @PostMapping("/topics/unsubscribe")
    public ResponseEntity<ApiResponse<Void>> unsubscribeFromTopic(
            @Parameter(name = "topic", description = "The topic to unsubscribe from", example = "general") @RequestParam String topic
    ) {
        messageService.unsubscribeFromTopic(topic);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Unsubscribed from topic " + topic + " successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the list of topics a user is subscribed to.
     *
     * @param userId the ID of the user
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a list of topic names as strings
     */
    @Operation(summary = "Get subscribed topics",
            description = "Returns the list of topics a user is subscribed to.")
    @GetMapping("/topics/subscribed")
    public ResponseEntity<ApiResponse<List<String>>> getSubscribedTopics(
            @Parameter(name = "userId", description = "ID of the user", example = "1") @RequestParam Long userId
    ) {
        List<String> topics = messageService.getSubscribedTopics(userId);
        ApiResponse<List<String>> response = ApiResponse.<List<String>>builder()
                .message("Subscribed topics retrieved successfully")
                .result(topics)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all messages in a specified group topic.
     *
     * @param topic the topic to retrieve messages from
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a list of {@link PrivateMessageResponse} objects
     */
    @Operation(summary = "Get messages in a topic",
            description = "Returns the list of messages in a group topic.")
    @GetMapping("/topics/{topic}/messages")
    public ResponseEntity<ApiResponse<List<PrivateMessageResponse>>> getMessagesInTopic(
            @Parameter(name = "topic", description = "The topic to retrieve messages from", example = "general") @PathVariable String topic
    ) {
        List<PrivateMessageResponse> messages = messageService.getMessagesInTopic(topic);
        ApiResponse<List<PrivateMessageResponse>> response = ApiResponse.<List<PrivateMessageResponse>>builder()
                .message("Messages in topic " + topic + " retrieved successfully")
                .result(messages)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the conversation history between two users.
     *
     * @param senderId   the ID of the user who sent messages
     * @param receiverId the ID of the user who received messages
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a list of {@link PrivateMessageResponse} objects representing the conversation
     * @implNote Delegates to {@link IMessageService} to fetch messages between the specified users.
     */
    @Operation(summary = "Get conversation between two users",
            description = "Returns the private messages exchanged between two users, ordered by timestamp.")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<PrivateMessageResponse>>> getMessagesBetweenUsers(
            @Parameter(name = "senderId", description = "ID of the sender", example = "1") @RequestParam Long senderId,
            @Parameter(name = "receiverId", description = "ID of the receiver", example = "2") @RequestParam Long receiverId
    ) {
        List<PrivateMessageResponse> messages = messageService.getMessagesBetweenUsers(senderId, receiverId);
        ApiResponse<List<PrivateMessageResponse>> response = ApiResponse.<List<PrivateMessageResponse>>builder()
                .message("Messages between users retrieved successfully")
                .result(messages)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all available topics.
     *
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a list of {@link TopicResponse} objects
     */
    @Operation(summary = "Get all topics",
            description = "Retrieves all available group topics that users can subscribe to.")
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