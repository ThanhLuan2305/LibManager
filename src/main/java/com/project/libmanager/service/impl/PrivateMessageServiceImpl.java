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
    private final PrivateMessageRepository repository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PrivateMessageRepository privateMessageRepository;
    private final UserRepository userRepository;

    @Override
    public List<PrivateMessageResponse> getMessagesBetweenUsers(Long senderId, Long recieverId) {
        List<PrivateMessage> messages = repository.findMessagesBetweenUsers(senderId, recieverId);
        return messages.stream().map(this::convertToResponse).toList();
    }

    @Override
    public PrivateMessageResponse sendMessagePrivate(PrivateMessageRequest request) {
        // Tạo và lưu tin nhắn vào database
        PrivateMessage message = PrivateMessage.builder()
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .content(request.getContent())
                .timestamp(Instant.now())
                .messageType(request.getMessageType())
                .build();

        message = privateMessageRepository.save(message);

        PrivateMessageResponse responseMessage = convertPrivateMessageToResponse(message);

        Long receiverId = request.getReceiverId();
        Long senderId = request.getSenderId();

        messagingTemplate.convertAndSend(
                "/queue/private/" + receiverId,
                responseMessage
        );

        if (receiverId == 1) {
            messagingTemplate.convertAndSend(
                    "/queue/private/admin",
                    responseMessage
            );
        }

        if (senderId == 1) {
            messagingTemplate.convertAndSend(
                    "/queue/private/" + receiverId,
                    responseMessage
            );
        }

        return responseMessage;
    }

    private PrivateMessageResponse convertPrivateMessageToResponse(PrivateMessage message) {
        return PrivateMessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .messageType(message.getMessageType())
                .build();
    }

    @Override
    public Map<Long, List<PrivateMessageResponse>> getMessAdminWithAllUser() {
        User admin = getAuthenticatedUser();
        Long adminId = admin.getId();

        List<User> allUsers = userRepository.findAll();
        List<User> normalUsers = allUsers.stream()
                .filter(user -> user.getRoles().stream()
                        .noneMatch(role -> role.getName().equals("ADMIN")))
                .toList();

        Map<Long, List<PrivateMessageResponse>> result = new HashMap<>();

        for (User user : normalUsers) {
            List<PrivateMessage> messages = privateMessageRepository
                    .findMessagesBetweenUsers(user.getId(), adminId);

            result.put(user.getId(), messages.stream()
                    .map(this::convertToResponse)
                    .toList());
        }

        return result;
    }


    private User getAuthenticatedUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null || context.getAuthentication() == null || !context.getAuthentication().isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String email = context.getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }


    @Override
    public void deleteMessage(String messageId) {
        repository.deleteById(messageId);
    }

    private PrivateMessageResponse convertToResponse(PrivateMessage message) {
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
