package com.project.libmanager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.entity.Message;
import com.project.libmanager.entity.TopicSubscription;
import com.project.libmanager.entity.UserConnection;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.repository.MessageRepository;
import com.project.libmanager.repository.TopicSubscriptionRepository;
import com.project.libmanager.repository.UserConnectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for managing real-time chat functionality.
 * Handles user connections, message processing, subscriptions, and disconnections.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketChatHandler extends TextWebSocketHandler {
    private static final String SUBSCRIBE_PREFIX = "subscribe:";  // Prefix for subscription commands
    private static final String UNSUBSCRIBE_PREFIX = "unsubscribe:";  // Prefix for unsubscription commands

    private final MessageRepository messageRepository;  // Repository for message persistence
    private final UserConnectionRepository connectionRepository;  // Repository for user connection status
    private final TopicSubscriptionRepository subscriptionRepository;  // Repository for topic subscriptions
    private final ObjectMapper objectMapper;  // JSON serializer/deserializer
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();  // Active WebSocket sessions by user ID

    /**
     * Handles a new WebSocket connection for a user.
     *
     * @param session the {@link WebSocketSession} established for the user
     * @throws Exception if an error occurs during connection setup or message sending
     * @implNote Validates user ID, updates connection status, and sends pending messages.
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserIdFromSession(session);
        // Validate user ID; anonymous users are not allowed
        if (!isNumeric(userId)) {
            try {
                session.sendMessage(new TextMessage("{\"error\":\"Authentication required. Anonymous users are not allowed.\"}"));
                session.close(CloseStatus.NOT_ACCEPTABLE);
            } catch (IOException e) {
                log.error("Failed to send error message or close session {}: {}", session.getId(), e.getMessage(), e);
            }
            log.warn("Anonymous user attempted to connect: {}", userId);
            return;
        }

        // Store session; enables message routing
        userSessions.put(userId, session);

        // Update connection status; creates or updates UserConnection entity
        UserConnection connection = connectionRepository.findById(userId)
                .orElse(UserConnection.builder()
                        .userId(userId)
                        .connected(true)
                        .lastConnectedTime(System.currentTimeMillis())
                        .build());
        connection.setConnected(true);
        connection.setLastConnectedTime(System.currentTimeMillis());
        connectionRepository.save(connection);

        log.info("User {} connected", userId);
        try {
            // Send pending messages and confirm connection
            sendPendingMessages(userId);
            session.sendMessage(new TextMessage("{\"status\":\"connected\",\"userId\":\"" + userId + "\"}"));
        } catch (IOException e) {
            log.error("Failed to send pending messages or connection status to user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Processes incoming WebSocket text messages.
     *
     * @param session the {@link WebSocketSession} that received the message
     * @param message the {@link TextMessage} containing the payload
     * @throws IOException  if an error occurs while sending responses
     * @throws AppException if:
     *                      - sender ID is invalid (ErrorCode.USER_NOT_EXISTED)
     *                      - message handling fails (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Routes JSON messages to handleJsonMessage and commands to handleCommandMessage.
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        String senderId = getUserIdFromSession(session);

        // Validate sender ID; ensures authenticated user
        if (!isNumeric(senderId)) {
            session.sendMessage(new TextMessage("{\"error\":\"Invalid sender ID. Authentication required.\"}"));
            log.warn("Invalid sender ID: {}", senderId);
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        log.info("Received message from {}: {}", senderId, payload);

        try {
            // Route message based on format; JSON for chats, commands for actions
            if (payload.startsWith("{")) {
                handleJsonMessage(payload, senderId, session);
            } else {
                handleCommandMessage(payload, senderId, session);
            }
        } catch (IOException e) {
            log.error("Failed to handle message from {}: {}. Error: {}", senderId, payload, e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        } catch (Exception e) {
            log.error("Unexpected error while handling message from {}: {}. Error: {}", senderId, payload, e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Handles JSON-formatted messages for private or group chats.
     *
     * @param payload  the JSON payload of the message
     * @param senderId the ID of the sender
     * @param session  the {@link WebSocketSession} of the sender
     * @throws IOException  if an error occurs while sending messages
     * @throws AppException if message format is invalid (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Deserializes JSON to Message and routes to handlePrivateChat or handleGroupChat.
     */
    private void handleJsonMessage(String payload, String senderId, WebSocketSession session) throws IOException {
        // Parse JSON payload into Message object
        Message msg = objectMapper.readValue(payload, Message.class);
        // Route based on message type; private if receiverId, group if topic
        if (msg.getReceiverId() != null) {
            handlePrivateChat(senderId, msg, session);
        } else if (msg.getTopic() != null) {
            handleGroupChat(senderId, msg, session);
        } else {
            session.sendMessage(new TextMessage("{\"error\":\"Invalid message format. Must specify receiverId or topic.\"}"));
            log.warn("Invalid message format from {}: {}", senderId, payload);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Handles command messages (subscribe, unsubscribe, read).
     *
     * @param payload  the command payload (e.g., "subscribe:topic")
     * @param senderId the ID of the sender
     * @param session  the {@link WebSocketSession} of the sender
     * @throws IOException  if an error occurs while sending responses
     * @throws AppException if command is invalid (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Routes to specific handlers based on command prefix.
     */
    private void handleCommandMessage(String payload, String senderId, WebSocketSession session) throws IOException {
        // Route command to appropriate handler
        if (payload.startsWith(SUBSCRIBE_PREFIX)) {
            handleSubscribe(senderId, payload, session);
        } else if (payload.startsWith(UNSUBSCRIBE_PREFIX)) {
            handleUnsubscribe(senderId, payload, session);
        } else if (payload.startsWith("read:")) {
            handleMarkAsRead(senderId, payload, session);
        } else {
            session.sendMessage(new TextMessage("{\"error\":\"Invalid command. Use: JSON message, subscribe, unsubscribe, read\"}"));
            log.warn("Invalid command from {}: {}", senderId, payload);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Handles WebSocket disconnection events.
     *
     * @param session the {@link WebSocketSession} that was closed
     * @param status  the {@link CloseStatus} indicating the reason for closure
     * @implNote Removes the session and updates the user's connection status.
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = getUserIdFromSession(session);
        // Remove session; prevents further message routing
        userSessions.remove(userId);

        // Update connection status; marks user as disconnected
        UserConnection connection = connectionRepository.findById(userId).orElse(null);
        if (connection != null) {
            connection.setConnected(false);
            connectionRepository.save(connection);
        }
        log.info("User {} disconnected", userId);
    }

    /**
     * Sends a message from the service layer via WebSocket.
     *
     * @param message the {@link Message} to send
     * @throws IOException  if an error occurs while sending the message
     * @throws AppException if the sender's session is unavailable (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Serializes the message to JSON and routes through handleTextMessage.
     */
    public void sendMessageFromService(Message message) throws IOException {
        String senderId = String.valueOf(message.getSenderId());
        WebSocketSession session = userSessions.get(senderId);
        // Validate session; ensures sender is connected
        if (session != null && session.isOpen()) {
            String messageJson = objectMapper.writeValueAsString(message);
            handleTextMessage(session, new TextMessage(messageJson));
        } else {
            log.warn("WebSocket session for sender {} is not available", senderId);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Triggers a subscription to a topic from the service layer.
     *
     * @param userId the ID of the user to subscribe
     * @param topic  the topic to subscribe to
     * @throws IOException  if an error occurs while sending the subscription command
     * @throws AppException if the user's session is unavailable (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Sends a "subscribe:topic" command through handleTextMessage.
     */
    public void triggerSubscribeFromService(String userId, String topic) throws IOException {
        WebSocketSession session = userSessions.get(userId);
        // Validate session; ensures user is connected
        if (session != null && session.isOpen()) {
            handleTextMessage(session, new TextMessage(SUBSCRIBE_PREFIX + topic));
        } else {
            log.warn("WebSocket session for user {} is not available", userId);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Triggers an unsubscription from a topic from the service layer.
     *
     * @param userId the ID of the user to unsubscribe
     * @param topic  the topic to unsubscribe from
     * @throws IOException  if an error occurs while sending the unsubscription command
     * @throws AppException if the user's session is unavailable (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Sends an "unsubscribe:topic" command through handleTextMessage.
     */
    public void triggerUnsubscribeFromService(String userId, String topic) throws IOException {
        WebSocketSession session = userSessions.get(userId);
        // Validate session; ensures user is connected
        if (session != null && session.isOpen()) {
            handleTextMessage(session, new TextMessage(UNSUBSCRIBE_PREFIX + topic));
        } else {
            log.warn("WebSocket session for user {} is not available", userId);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Handles sending a private chat message.
     *
     * @param senderId      the ID of the sender
     * @param message       the {@link Message} to send
     * @param senderSession the {@link WebSocketSession} of the sender
     * @throws IOException  if an error occurs while sending the message
     * @throws AppException if:
     *                      - sender ID mismatch (ErrorCode.UNCATEGORIZED_EXCEPTION)
     *                      - receiver ID is invalid (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Validates sender and receiver, persists message, and delivers to receiver if online.
     */
    private void handlePrivateChat(String senderId, Message message, WebSocketSession senderSession) throws IOException {
        // Validate sender ID; ensures message integrity
        if (!senderId.equals(String.valueOf(message.getSenderId()))) {
            senderSession.sendMessage(new TextMessage("{\"error\":\"Sender ID does not match the message.\"}"));
            log.warn("Sender ID mismatch: expected {}, got {}", senderId, message.getSenderId());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // Validate receiver ID; ensures numeric format
        String receiverId = String.valueOf(message.getReceiverId());
        if (!isNumeric(receiverId)) {
            senderSession.sendMessage(new TextMessage("{\"error\":\"Receiver ID must be numeric.\"}"));
            log.warn("Receiver ID {} is not numeric", receiverId);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // Check for duplicate message; prevents redundant storage
        List<Message> recentMessages = messageRepository.findMessagesBetweenUsers(Long.parseLong(senderId), Long.parseLong(receiverId));
        Message existingMessage = recentMessages.stream()
                .filter(msg -> msg.getContent().equals(message.getContent()) && msg.getSenderId().equals(Long.parseLong(senderId)))
                .max((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()))
                .orElse(null);

        // Persist message if new; avoids duplicates
        if (existingMessage == null) {
            messageRepository.save(message);
            existingMessage = message;
        }

        String messageJson = objectMapper.writeValueAsString(existingMessage);
        log.info("Sending private message from {} to {}: {}", senderId, receiverId, messageJson);

        // Deliver message if receiver is online; otherwise notify sender
        if (connectionRepository.existsByUserIdAndConnectedTrue(receiverId)) {
            WebSocketSession receiverSession = userSessions.get(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.sendMessage(new TextMessage(messageJson));
                senderSession.sendMessage(new TextMessage(messageJson));
                existingMessage.setDelivered(true);
                messageRepository.save(existingMessage);
                log.info("Private message delivered to {}", receiverId);
            } else {
                log.warn("Receiver {} session not open", receiverId);
            }
        } else {
            senderSession.sendMessage(new TextMessage("{\"status\":\"offline\",\"receiverId\":\"" + receiverId + "\"}"));
            log.info("Receiver {} is offline", receiverId);
        }
    }

    /**
     * Handles sending a group chat message to a topic.
     *
     * @param senderId      the ID of the sender
     * @param message       the {@link Message} to send
     * @param senderSession the {@link WebSocketSession} of the sender
     * @throws IOException  if an error occurs while sending the message
     * @throws AppException if:
     *                      - sender ID mismatch (ErrorCode.UNCATEGORIZED_EXCEPTION)
     *                      - user not subscribed to topic (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Validates subscription, persists message, and delivers to all subscribers.
     */
    private void handleGroupChat(String senderId, Message message, WebSocketSession senderSession) throws IOException {
        // Validate sender ID; ensures message integrity
        if (!senderId.equals(String.valueOf(message.getSenderId()))) {
            senderSession.sendMessage(new TextMessage("{\"error\":\"Sender ID does not match the message.\"}"));
            log.warn("Sender ID mismatch: expected {}, got {}", senderId, message.getSenderId());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        String topic = message.getTopic();
        // Validate subscription; ensures user is subscribed to topic
        if (!subscriptionRepository.existsByUserIdAndTopic(senderId, topic)) {
            senderSession.sendMessage(new TextMessage("{\"error\":\"You must subscribe to " + topic + " first\"}"));
            log.warn("User {} not subscribed to topic {}", senderId, topic);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // Check for duplicate message; prevents redundant storage
        List<Message> recentMessages = messageRepository.findByTopic(topic);
        Message existingMessage = recentMessages.stream()
                .filter(msg -> msg.getContent().equals(message.getContent()) && msg.getSenderId().equals(Long.parseLong(senderId)))
                .max((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()))
                .orElse(null);

        // Persist message if new; avoids duplicates
        if (existingMessage == null) {
            messageRepository.save(message);
            existingMessage = message;
        }

        String messageJson = objectMapper.writeValueAsString(existingMessage);
        log.info("Sending group message from {} to topic {}: {}", senderId, topic, messageJson);

        // Deliver to all subscribers; marks as delivered if at least one receives it
        List<TopicSubscription> subscriptions = subscriptionRepository.findByTopic(topic);
        boolean atLeastOneDelivered = false;
        for (TopicSubscription sub : subscriptions) {
            String userId = sub.getUserId();
            if (connectionRepository.existsByUserIdAndConnectedTrue(userId)) {
                WebSocketSession session = userSessions.get(userId);
                if (session != null && session.isOpen()) {
                    session.sendMessage(new TextMessage(messageJson));
                    atLeastOneDelivered = true;
                    log.info("Group message delivered to {}", userId);
                }
            }
        }
        if (atLeastOneDelivered) {
            existingMessage.setDelivered(true);
            messageRepository.save(existingMessage);
        } else {
            log.info("No subscribers online for topic {}", topic);
        }
    }

    /**
     * Handles a subscription request to a topic.
     *
     * @param userId  the ID of the user subscribing
     * @param payload the subscription command (e.g., "subscribe:topic")
     * @param session the {@link WebSocketSession} of the user
     * @throws IOException if an error occurs while sending messages
     * @implNote Persists subscription and sends pending topic messages.
     */
    private void handleSubscribe(String userId, String payload, WebSocketSession session) throws IOException {
        String topic = payload.substring(SUBSCRIBE_PREFIX.length());
        // Persist subscription if new; avoids duplicates
        if (!subscriptionRepository.existsByUserIdAndTopic(userId, topic)) {
            TopicSubscription subscription = TopicSubscription.builder()
                    .userId(userId)
                    .topic(topic)
                    .build();
            subscriptionRepository.save(subscription);
            log.info("User {} subscribed to topic {}", userId, topic);
        }
        session.sendMessage(new TextMessage("{\"status\":\"subscribed\",\"topic\":\"" + topic + "\"}"));

        // Send pending messages for the topic; marks as delivered
        List<Message> pendingMessages = messageRepository.findByTopicAndDeliveredFalse(topic);
        for (Message msg : pendingMessages) {
            String messageJson = objectMapper.writeValueAsString(msg);
            session.sendMessage(new TextMessage(messageJson));
            msg.setDelivered(true);
            messageRepository.save(msg);
            log.info("Sent pending message {} to {}", msg.getId(), userId);
        }
    }

    /**
     * Handles an unsubscription request from a topic.
     *
     * @param userId  the ID of the user unsubscribing
     * @param payload the unsubscription command (e.g., "unsubscribe:topic")
     * @param session the {@link WebSocketSession} of the user
     * @throws IOException if an error occurs while sending the response
     * @implNote Removes the subscription and notifies the user.
     */
    private void handleUnsubscribe(String userId, String payload, WebSocketSession session) throws IOException {
        String topic = payload.substring(UNSUBSCRIBE_PREFIX.length());
        // Remove subscription; stops message delivery for the topic
        subscriptionRepository.deleteByUserIdAndTopic(userId, topic);
        session.sendMessage(new TextMessage("{\"status\":\"unsubscribed\",\"topic\":\"" + topic + "\"}"));
        log.info("User {} unsubscribed from topic {}", userId, topic);
    }

    /**
     * Handles marking a message as read.
     *
     * @param userId  the ID of the user marking the message
     * @param payload the read command (e.g., "read:messageId")
     * @param session the {@link WebSocketSession} of the user
     * @throws IOException  if an error occurs while sending the response
     * @throws AppException if message is not found or user is unauthorized (ErrorCode.UNCATEGORIZED_EXCEPTION)
     * @implNote Updates message status and notifies the user.
     */
    private void handleMarkAsRead(String userId, String payload, WebSocketSession session) throws IOException {
        String messageId = payload.substring("read:".length());
        Message message = messageRepository.findById(messageId).orElse(null);
        // Validate message and authorization; ensures user can mark as read
        if (message != null && (message.getReceiverId().equals(Long.parseLong(userId)) || message.getTopic() != null)) {
            message.setRead(true);
            messageRepository.save(message);
            session.sendMessage(new TextMessage("{\"status\":\"read\",\"messageId\":\"" + messageId + "\"}"));
            log.info("Message {} marked as read by {}", messageId, userId);
        } else {
            session.sendMessage(new TextMessage("{\"error\":\"Message not found or not authorized\"}"));
            log.warn("User {} tried to mark invalid message {} as read", userId, messageId);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Sends pending messages to a user upon connection.
     *
     * @param userId the ID of the user to send messages to
     * @throws IOException if an error occurs while sending messages
     * @implNote Sends undelivered private and group messages, marking them as delivered.
     */
    private void sendPendingMessages(String userId) throws IOException {
        WebSocketSession session = userSessions.get(userId);
        // Validate session; ensures user is connected
        if (session == null || !session.isOpen()) {
            log.warn("Session for user {} not available", userId);
            return;
        }

        // Send pending private messages; marks as delivered
        List<Message> pendingPrivate = messageRepository.findByReceiverIdAndDeliveredFalse(Long.parseLong(userId));
        for (Message msg : pendingPrivate) {
            String messageJson = objectMapper.writeValueAsString(msg);
            session.sendMessage(new TextMessage(messageJson));
            msg.setDelivered(true);
            messageRepository.save(msg);
            log.info("Sent pending private message {} to {}", msg.getId(), userId);
        }

        // Send pending group messages for subscribed topics; marks as delivered
        List<TopicSubscription> subscriptions = subscriptionRepository.findAll().stream()
                .filter(sub -> sub.getUserId().equals(userId))
                .toList();
        for (TopicSubscription sub : subscriptions) {
            List<Message> pendingGroup = messageRepository.findByTopicAndDeliveredFalse(sub.getTopic());
            for (Message msg : pendingGroup) {
                String messageJson = objectMapper.writeValueAsString(msg);
                session.sendMessage(new TextMessage(messageJson));
                msg.setDelivered(true);
                messageRepository.save(msg);
                log.info("Sent pending group message {} to {} in topic {}", msg.getId(), userId, sub.getTopic());
            }
        }
    }

    /**
     * Extracts the user ID from a WebSocket session.
     *
     * @param session the {@link WebSocketSession} to extract the user ID from
     * @return the user ID as a string, or an anonymous identifier if not found
     * @implNote Retrieves userId from session attributes; defaults to anonymous if missing.
     */
    private String getUserIdFromSession(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        return userId != null ? userId.toString() : "anonymous_" + session.getId();
    }

    /**
     * Checks if a string is numeric.
     *
     * @param str the string to check
     * @return true if the string is numeric, false otherwise
     * @implNote Uses regex to validate that the string contains only digits.
     */
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        return str.matches("\\d+");
    }

    /**
     * Retrieves the WebSocket session for a user.
     *
     * @param userId the ID of the user
     * @return the {@link WebSocketSession} associated with the user, or null if not found
     * @implNote Looks up the session in the userSessions map.
     */
    public WebSocketSession getUserSession(String userId) {
        return userSessions.get(userId);
    }
}