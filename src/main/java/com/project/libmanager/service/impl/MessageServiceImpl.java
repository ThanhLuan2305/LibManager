package com.project.libmanager.service.impl;

import com.project.libmanager.config.WebSocketChatHandler;
import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.entity.Message;
import com.project.libmanager.entity.Topic;
import com.project.libmanager.entity.TopicSubscription;
import com.project.libmanager.entity.User;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.repository.MessageRepository;
import com.project.libmanager.repository.TopicRepository;
import com.project.libmanager.repository.TopicSubscriptionRepository;
import com.project.libmanager.repository.UserRepository;
import com.project.libmanager.service.IMessageService;
import com.project.libmanager.service.dto.request.PrivateMessageRequest;
import com.project.libmanager.service.dto.response.PrivateMessageResponse;
import com.project.libmanager.service.dto.response.TopicResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for managing messages, topics, and subscriptions.
 * Handles private and group messaging, topic management, and user subscriptions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements IMessageService {

    private final MessageRepository messageRepository;  // Repository for message persistence
    private final UserRepository userRepository;  // Repository for user data
    private final TopicRepository topicRepository;  // Repository for topic data
    private final TopicSubscriptionRepository subscriptionRepository;  // Repository for topic subscriptions
    private final WebSocketChatHandler chatHandler;  // WebSocket handler for real-time messaging

    private static final String ROLE_ADMIN = "ADMIN";  // Role identifier for admin users

    /**
     * Retrieves all messages exchanged between two users.
     *
     * @param senderId   the ID of the sender
     * @param receiverId the ID of the receiver
     * @return a list of {@link PrivateMessageResponse} objects representing the messages
     * @throws AppException if either user does not exist (ErrorCode.USER_NOT_EXISTED)
     * @implNote Fetches messages from the repository and converts them to response DTOs.
     */
    @Override
    public List<PrivateMessageResponse> getMessagesBetweenUsers(Long senderId, Long receiverId) {
        // Validate users; ensures both sender and receiver exist
        userRepository.findById(senderId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userRepository.findById(receiverId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Fetch messages; retrieves conversation history
        List<Message> messages = messageRepository.findMessagesBetweenUsers(senderId, receiverId);
        // Convert to response DTOs; maps entities to DTOs for API response
        return messages.stream().map(this::convertToResponse).toList();
    }

    /**
     * Sends a private message from one user to another.
     *
     * @param request the {@link PrivateMessageRequest} containing message details
     * @return a {@link PrivateMessageResponse} representing the sent message
     * @throws AppException if:
     *                      - sender or receiver does not exist (ErrorCode.USER_NOT_EXISTED)
     *                      - message content is invalid (ErrorCode.INVALID_MESSAGE_CONTENT)
     * @throws IOException  if WebSocket message sending fails
     * @implNote Persists the message and sends it via WebSocket.
     */
    @Override
    public PrivateMessageResponse sendMessagePrivate(PrivateMessageRequest request) throws IOException {
        // Validate users; ensures sender and receiver exist
        userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        // Validate content; ensures message is not empty
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_MESSAGE_CONTENT);
        }

        // Build message entity; sets initial status as undelivered and unread
        Message message = Message.builder()
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .content(request.getContent())
                .timestamp(Instant.now())
                .delivered(false)
                .read(false)
                .build();

        // Persist message; saves to database
        message = messageRepository.save(message);
        // Convert to response DTO; prepares API response
        PrivateMessageResponse response = convertToResponse(message);

        // Send via WebSocket; delivers message in real-time
        try {
            chatHandler.sendMessageFromService(message);
        } catch (Exception e) {
            log.error("Error sending private message from {} to {}: {}", request.getSenderId(), request.getReceiverId(), e.getMessage(), e);
            throw new IOException("Failed to send private message via WebSocket", e);
        }

        return response;
    }

    /**
     * Sends a group message to a specified topic.
     *
     * @param senderId the ID of the sender
     * @param topic    the topic to send the message to
     * @param content  the message content
     * @return a {@link PrivateMessageResponse} representing the sent message
     * @throws AppException if:
     *                      - sender does not exist (ErrorCode.USER_NOT_EXISTED)
     *                      - topic is invalid (ErrorCode.INVALID_TOPIC)
     *                      - topic does not exist (ErrorCode.TOPIC_NOT_EXISTED)
     *                      - user is not subscribed (ErrorCode.NOT_SUBSCRIBED)
     *                      - content is invalid (ErrorCode.INVALID_MESSAGE_CONTENT)
     * @throws IOException  if WebSocket message sending fails
     * @implNote Persists the message and sends it to all topic subscribers via WebSocket.
     */
    @Override
    public PrivateMessageResponse sendGroupMessage(Long senderId, String topic, String content) throws IOException {
        // Validate sender; ensures user exists
        userRepository.findById(senderId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Validate topic; ensures it is not empty
        if (topic == null || topic.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_TOPIC);
        }

        // Validate topic existence; ensures topic is created
        if (!topicRepository.existsByName(topic)) {
            throw new AppException(ErrorCode.TOPIC_NOT_EXISTED);
        }

        // Validate subscription; ensures user is subscribed to topic
        if (!subscriptionRepository.existsByUserIdAndTopic(String.valueOf(senderId), topic)) {
            throw new AppException(ErrorCode.NOT_SUBSCRIBED);
        }

        // Validate content; ensures message is not empty
        if (content == null || content.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_MESSAGE_CONTENT);
        }

        // Build message entity; sets initial status as undelivered and unread
        Message message = Message.builder()
                .senderId(senderId)
                .topic(topic)
                .content(content)
                .timestamp(Instant.now())
                .delivered(false)
                .read(false)
                .build();

        // Persist message; saves to database
        message = messageRepository.save(message);
        // Convert to response DTO; prepares API response
        PrivateMessageResponse response = convertToResponse(message);

        // Send via WebSocket; delivers message to topic subscribers
        try {
            chatHandler.sendMessageFromService(message);
        } catch (Exception e) {
            log.error("Error sending group message to topic {}: {}", topic, e.getMessage(), e);
            throw new IOException("Failed to send group message via WebSocket", e);
        }

        return response;
    }

    /**
     * Retrieves all messages between the admin and all non-admin users.
     *
     * @return a map of user IDs to their message lists with the admin
     * @throws AppException if the authenticated admin user cannot be retrieved (ErrorCode.USER_NOT_EXISTED)
     * @implNote Filters non-admin users and fetches their conversations with the admin.
     */
    @Override
    public Map<Long, List<PrivateMessageResponse>> getMessAdminWithAllUser() {
        // Get authenticated admin; ensures admin is logged in
        User admin = getAuthenticatedUser();
        Long adminId = admin.getId();

        // Fetch all users; includes both admin and non-admin
        List<User> allUsers = userRepository.findAll();
        // Filter non-admin users; excludes users with ADMIN role
        List<User> normalUsers = allUsers.stream()
                .filter(user -> user.getRoles().stream()
                        .noneMatch(role -> role.getName().equals(ROLE_ADMIN)))
                .toList();

        // Build result map; maps user IDs to their messages with admin
        Map<Long, List<PrivateMessageResponse>> result = new HashMap<>();
        for (User user : normalUsers) {
            List<Message> messages = messageRepository.findMessagesBetweenUsers(user.getId(), adminId);
            result.put(user.getId(), messages.stream().map(this::convertToResponse).toList());
        }
        return result;
    }

    /**
     * Subscribes the authenticated user to a topic.
     *
     * @param topic the topic to subscribe to
     * @throws AppException if:
     *                      - topic is invalid (ErrorCode.INVALID_TOPIC)
     *                      - topic does not exist (ErrorCode.TOPIC_NOT_EXISTED)
     *                      - user is already subscribed (ErrorCode.ALREADY_SUBSCRIBED)
     *                      - authenticated user cannot be retrieved (ErrorCode.USER_NOT_EXISTED)
     * @implNote Persists the subscription and triggers WebSocket subscription.
     */
    @Override
    public void subscribeToTopic(String topic) {
        // Get authenticated user; ensures user is logged in
        User user = getAuthenticatedUser();

        // Validate topic; ensures it is not empty
        if (topic == null || topic.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_TOPIC);
        }

        // Validate topic existence; ensures topic is created
        if (!topicRepository.existsByName(topic)) {
            throw new AppException(ErrorCode.TOPIC_NOT_EXISTED);
        }

        // Check for existing subscription; prevents duplicates
        if (subscriptionRepository.existsByUserIdAndTopic(String.valueOf(user.getId()), topic)) {
            throw new AppException(ErrorCode.ALREADY_SUBSCRIBED);
        }

        // Persist subscription; adds user to topic
        TopicSubscription subscription = TopicSubscription.builder()
                .userId(String.valueOf(user.getId()))
                .topic(topic)
                .build();
        subscriptionRepository.save(subscription);
        log.info("User {} subscribed to topic {}", user.getId(), topic);

        // Trigger WebSocket subscription; notifies client in real-time
        try {
            chatHandler.triggerSubscribeFromService(String.valueOf(user.getId()), topic);
        } catch (IOException e) {
            log.error("Failed to trigger subscription for user {} to topic {}", user.getId(), topic, e);
        }
    }

    /**
     * Unsubscribes the authenticated user from a topic.
     *
     * @param topic the topic to unsubscribe from
     * @throws AppException if:
     *                      - topic is invalid (ErrorCode.INVALID_TOPIC)
     *                      - topic does not exist (ErrorCode.TOPIC_NOT_EXISTED)
     *                      - user is not subscribed (ErrorCode.NOT_SUBSCRIBED)
     *                      - authenticated user cannot be retrieved (ErrorCode.USER_NOT_EXISTED)
     * @implNote Removes the subscription and triggers WebSocket unsubscription.
     */
    @Override
    public void unsubscribeFromTopic(String topic) {
        // Get authenticated user; ensures user is logged in
        User user = getAuthenticatedUser();

        // Validate topic; ensures it is not empty
        if (topic == null || topic.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_TOPIC);
        }

        // Validate topic existence; ensures topic is created
        if (!topicRepository.existsByName(topic)) {
            throw new AppException(ErrorCode.TOPIC_NOT_EXISTED);
        }

        // Check subscription; ensures user is subscribed
        if (!subscriptionRepository.existsByUserIdAndTopic(String.valueOf(user.getId()), topic)) {
            throw new AppException(ErrorCode.NOT_SUBSCRIBED);
        }

        // Remove subscription; stops message delivery for the topic
        subscriptionRepository.deleteByUserIdAndTopic(String.valueOf(user.getId()), topic);
        log.info("User {} unsubscribed from topic {}", user.getId(), topic);

        // Trigger WebSocket unsubscription; notifies client in real-time
        try {
            chatHandler.triggerUnsubscribeFromService(String.valueOf(user.getId()), topic);
        } catch (IOException e) {
            log.error("Failed to trigger unsubscription for user {} from topic {}", user.getId(), topic, e);
        }
    }

    /**
     * Retrieves all topics a user is subscribed to.
     *
     * @param userId the ID of the user
     * @return a list of topic names the user is subscribed to
     * @throws AppException if the user does not exist (ErrorCode.USER_NOT_EXISTED)
     * @implNote Fetches subscriptions from the repository and extracts topic names.
     */
    @Override
    public List<String> getSubscribedTopics(Long userId) {
        // Validate user; ensures user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Fetch subscriptions; retrieves all topics for the user
        List<TopicSubscription> subscriptions = subscriptionRepository.findByUserId(String.valueOf(userId));
        // Extract topic names; maps subscriptions to topic names
        return subscriptions.stream()
                .map(TopicSubscription::getTopic)
                .toList();
    }

    /**
     * Retrieves all messages in a specified topic.
     *
     * @param topic the topic to fetch messages from
     * @return a list of {@link PrivateMessageResponse} objects representing the messages
     * @throws AppException if:
     *                      - topic is invalid (ErrorCode.INVALID_TOPIC)
     *                      - topic does not exist (ErrorCode.TOPIC_NOT_EXISTED)
     *                      - user is not subscribed (ErrorCode.NOT_SUBSCRIBED)
     *                      - authenticated user cannot be retrieved (ErrorCode.USER_NOT_EXISTED)
     * @implNote Fetches messages from the repository and converts them to response DTOs.
     */
    @Override
    public List<PrivateMessageResponse> getMessagesInTopic(String topic) {
        // Get authenticated user; ensures user is logged in
        User user = getAuthenticatedUser();

        // Validate topic; ensures it is not empty
        if (topic == null || topic.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_TOPIC);
        }

        // Validate topic existence; ensures topic is created
        if (!topicRepository.existsByName(topic)) {
            throw new AppException(ErrorCode.TOPIC_NOT_EXISTED);
        }

        // Check subscription; ensures user is subscribed
        if (!subscriptionRepository.existsByUserIdAndTopic(String.valueOf(user.getId()), topic)) {
            throw new AppException(ErrorCode.NOT_SUBSCRIBED);
        }

        // Fetch messages; retrieves all messages in the topic
        List<Message> messages = messageRepository.findByTopic(topic);
        // Convert to response DTOs; maps entities to DTOs for API response
        return messages.stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * Creates a new topic for group messaging.
     *
     * @param topicName   the name of the topic
     * @param description the description of the topic (optional)
     * @throws AppException if:
     *                      - topic name is invalid (ErrorCode.INVALID_TOPIC)
     *                      - topic already exists (ErrorCode.TOPIC_ALREADY_EXISTS)
     * @implNote Persists the topic with admin as the creator.
     */
    @Override
    public void createTopic(String topicName, String description) {
        // Validate topic name; ensures it is not empty
        if (topicName == null || topicName.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_TOPIC);
        }

        // Check for existing topic; prevents duplicates
        if (topicRepository.existsByName(topicName)) {
            throw new AppException(ErrorCode.TOPIC_ALREADY_EXISTS);
        }

        // Build topic entity; sets admin as creator and default description if none provided
        Topic topic = new Topic();
        topic.setName(topicName);
        topic.setCreatedBy(ROLE_ADMIN);
        topic.setDescription(description != null ? description : "Group chat for " + topicName);
        // Persist topic; saves to database
        topicRepository.save(topic);
        log.info("Topic {} created by admin {}", topicName, ROLE_ADMIN);
    }

    /**
     * Removes a user from a topic (admin-only).
     *
     * @param userId the ID of the user to remove
     * @param topic  the topic to remove the user from
     * @throws AppException if:
     *                      - user does not exist (ErrorCode.USER_NOT_EXISTED)
     *                      - topic is invalid (ErrorCode.INVALID_TOPIC)
     *                      - topic does not exist (ErrorCode.TOPIC_NOT_EXISTED)
     *                      - user is not subscribed (ErrorCode.NOT_SUBSCRIBED)
     *                      - authenticated user is not an admin (ErrorCode.UNAUTHORIZED)
     *                      - authenticated admin cannot be retrieved (ErrorCode.USER_NOT_EXISTED)
     * @implNote Removes the subscription and triggers WebSocket unsubscription.
     */
    @Override
    public void removeUserFromTopic(Long userId, String topic) {
        // Validate user; ensures user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Validate topic; ensures it is not empty
        if (topic == null || topic.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_TOPIC);
        }

        // Validate topic existence; ensures topic is created
        if (!topicRepository.existsByName(topic)) {
            throw new AppException(ErrorCode.TOPIC_NOT_EXISTED);
        }

        // Validate admin privileges; ensures only admins can remove users
        User admin = getAuthenticatedUser();
        if (admin.getRoles().stream().noneMatch(role -> role.getName().equals(ROLE_ADMIN))) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Check subscription; ensures user is subscribed
        if (subscriptionRepository.existsByUserIdAndTopic(String.valueOf(userId), topic)) {
            // Remove subscription; stops message delivery for the user
            subscriptionRepository.deleteByUserIdAndTopic(String.valueOf(userId), topic);
            log.info("User {} removed from topic {} by admin {}", userId, topic, admin.getId());

            // Trigger WebSocket unsubscription; notifies client in real-time
            try {
                chatHandler.triggerUnsubscribeFromService(String.valueOf(userId), topic);
            } catch (IOException e) {
                log.error("Failed to trigger removal notification for user {} from topic {}", userId, topic, e);
            }
        } else {
            throw new AppException(ErrorCode.NOT_SUBSCRIBED);
        }
    }

    /**
     * Retrieves all available topics.
     *
     * @return a list of {@link TopicResponse} objects representing all topics
     * @implNote Fetches all topics from the repository and converts them to response DTOs.
     */
    @Override
    public List<TopicResponse> getAllTopics() {
        // Fetch all topics; retrieves all topic entities
        return topicRepository.findAll().stream()
                // Convert to response DTOs; maps entities to DTOs for API response
                .map(topic -> TopicResponse.builder()
                        .name(topic.getName())
                        .createdBy(topic.getCreatedBy())
                        .description(topic.getDescription())
                        .build())
                .toList();
    }

    /**
     * Retrieves the authenticated user from the security context.
     *
     * @return the authenticated {@link User}
     * @throws AppException if the user cannot be found (ErrorCode.USER_NOT_EXISTED)
     * @implNote Extracts the email from the security context and fetches the user.
     */
    private User getAuthenticatedUser() {
        // Extract email from security context; identifies the logged-in user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // Fetch user by email; ensures user exists
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    /**
     * Converts a message entity to a response DTO.
     *
     * @param message the {@link Message} entity to convert
     * @return a {@link PrivateMessageResponse} representing the message
     * @implNote Maps message fields to the response DTO.
     */
    private PrivateMessageResponse convertToResponse(Message message) {
        // Build response DTO; includes essential message details
        return PrivateMessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .build();
    }
}