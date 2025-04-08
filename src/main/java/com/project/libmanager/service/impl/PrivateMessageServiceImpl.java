package com.project.libmanager.service.impl;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.entity.PrivateMessage;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.repository.PrivateMessageRepository;
import com.project.libmanager.repository.UserRepository;
import com.project.libmanager.service.IPrivateMessageService;
import com.project.libmanager.service.dto.request.PrivateMessageRequest;
import com.project.libmanager.service.dto.response.PrivateMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class PrivateMessageServiceImpl implements IPrivateMessageService {

    private final PrivateMessageRepository repository;         // Repository for private message CRUD operations
    private final SimpMessagingTemplate messagingTemplate;     // Template for sending WebSocket messages
    private final PrivateMessageRepository privateMessageRepository; // Repository for private message operations
    private final UserRepository userRepository;               // Repository for user CRUD operations

    /**
     * Retrieves the list of messages exchanged between two users.
     *
     * @param senderId   the ID of the user who sent the messages
     * @param receiverId the ID of the user who received the messages
     * @return a {@link List} of {@link PrivateMessageResponse} objects representing the messages
     * @implNote Queries the repository for messages between the specified sender and receiver,
     * then maps each message to a response DTO using the convertToResponse method.
     */
    @Override
    public List<PrivateMessageResponse> getMessagesBetweenUsers(Long senderId, Long receiverId) {
        // Fetch messages between the two users from the repository
        List<PrivateMessage> messages = repository.findMessagesBetweenUsers(senderId, receiverId);
        // Convert each message to a response DTO and collect into a list
        return messages.stream().map(this::convertToResponse).toList();
    }

    /**
     * Sends a private message between users and broadcasts it via WebSocket.
     *
     * @param request a {@link PrivateMessageRequest} containing sender ID, receiver ID, content, and message type
     * @return a {@link PrivateMessageResponse} representing the sent message
     * @throws AppException if the message cannot be saved to the database
     * @implNote Builds and saves a PrivateMessage entity, converts it to a response DTO,
     * and sends it to the receiver's WebSocket queue. Special handling is applied for admin messages.
     */
    @Override
    public PrivateMessageResponse sendMessagePrivate(PrivateMessageRequest request) {
        // Build a new PrivateMessage entity from the request data
        PrivateMessage message = PrivateMessage.builder()
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .content(request.getContent())
                .timestamp(Instant.now())
                .messageType(request.getMessageType())
                .build();

        // Save the message to the database and update the entity with generated ID
        message = privateMessageRepository.save(message);

        // Convert the saved message to a response DTO
        PrivateMessageResponse responseMessage = convertPrivateMessageToResponse(message);

        // Extract receiver and sender IDs for WebSocket routing
        Long receiverId = request.getReceiverId();
        Long senderId = request.getSenderId();

        // Send the message to the receiver's private queue via WebSocket
        messagingTemplate.convertAndSend(
                "/queue/private/" + receiverId,
                responseMessage
        );

        // If the receiver is admin (ID = 1), also send to the admin queue
        if (receiverId == 1) {
            messagingTemplate.convertAndSend(
                    "/queue/private/admin",
                    responseMessage
            );
        }

        // If the sender is admin (ID = 1), send to the receiver's queue again for consistency
        if (senderId == 1) {
            messagingTemplate.convertAndSend(
                    "/queue/private/" + receiverId,
                    responseMessage
            );
        }

        // Return the response DTO to the caller
        return responseMessage;
    }

    /**
     * Converts a PrivateMessage entity to a PrivateMessageResponse DTO.
     *
     * @param message the {@link PrivateMessage} entity to convert
     * @return a {@link PrivateMessageResponse} containing the message details
     * @implNote Uses the builder pattern to construct a response DTO from the message entity.
     */
    private PrivateMessageResponse convertPrivateMessageToResponse(PrivateMessage message) {
        // Build and return a response DTO with all message fields
        return PrivateMessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .messageType(message.getMessageType())
                .build();
    }

    /**
     * Retrieves all messages between the admin and all non-admin users.
     *
     * @return a {@link Map} where the key is the user ID and the value is a list of
     * {@link PrivateMessageResponse} objects representing messages with the admin
     * @throws AppException if the authenticated user cannot be retrieved (via getAuthenticatedUser)
     * @implNote Fetches the admin user, filters out non-admin users, and constructs a map
     * of user IDs to their message history with the admin.
     */
    @Override
    public Map<Long, List<PrivateMessageResponse>> getMessAdminWithAllUser() {
        // Get the authenticated admin user
        User admin = getAuthenticatedUser();
        // Extract admin ID for message filtering
        Long adminId = admin.getId();

        // Fetch all users from the repository
        List<User> allUsers = userRepository.findAll();
        // Filter out users who do not have the ADMIN role
        List<User> normalUsers = allUsers.stream()
                .filter(user -> user.getRoles().stream()
                        .noneMatch(role -> role.getName().equals("ADMIN")))
                .toList();

        // Initialize a map to store user ID to message list mappings
        Map<Long, List<PrivateMessageResponse>> result = new HashMap<>();

        // Iterate over each normal user
        for (User user : normalUsers) {
            // Fetch messages between the current user and admin
            List<PrivateMessage> messages = privateMessageRepository
                    .findMessagesBetweenUsers(user.getId(), adminId);
            // Convert messages to response DTOs and add to the result map
            result.put(user.getId(), messages.stream()
                    .map(this::convertToResponse)
                    .toList());
        }

        // Return the completed map
        return result;
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     *
     * @return a {@link User} entity representing the authenticated user
     * @throws AppException if:
     *                      - no security context or authentication exists (ErrorCode.UNAUTHORIZED)
     *                      - authenticated user not found (ErrorCode.USER_NOT_EXISTED)
     * @implNote Extracts the email from the security context and queries the user repository
     * to return the corresponding user.
     */
    private User getAuthenticatedUser() {
        // Get current security context holding authentication details
        SecurityContext context = SecurityContextHolder.getContext();
        // Check if context or authentication is invalid or user is not authenticated
        if (context == null || context.getAuthentication() == null || !context.getAuthentication().isAuthenticated()) {
            // Throw exception if authentication is missing or invalid
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Extract email from authentication object
        String email = context.getAuthentication().getName();
        // Fetch user by email from database and throw exception if not found
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    /**
     * Converts a PrivateMessage entity to a PrivateMessageResponse DTO for response purposes.
     *
     * @param message the {@link PrivateMessage} entity to convert
     * @return a {@link PrivateMessageResponse} containing the message details
     * @implNote Uses the builder pattern to create a response DTO from the message entity.
     */
    private PrivateMessageResponse convertToResponse(PrivateMessage message) {
        // Build and return a response DTO with all message fields
        return PrivateMessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .messageType(message.getMessageType())
                .build();
    }
}