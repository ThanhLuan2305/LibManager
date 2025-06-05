package com.project.libmanager.service;

import com.project.libmanager.service.dto.request.PrivateMessageRequest;
import com.project.libmanager.service.dto.response.PrivateMessageResponse;
import com.project.libmanager.service.dto.response.TopicResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IMessageService {
    List<PrivateMessageResponse> getMessagesBetweenUsers(Long senderId, Long receiverId);

    PrivateMessageResponse sendMessagePrivate(PrivateMessageRequest request) throws IOException;

    PrivateMessageResponse sendGroupMessage(Long senderId, String topic, String content) throws IOException;

    Map<Long, List<PrivateMessageResponse>> getMessAdminWithAllUser();

    void subscribeToTopic(String topic);

    void unsubscribeFromTopic(String topic);

    List<String> getSubscribedTopics(Long userId);

    List<PrivateMessageResponse> getMessagesInTopic(String topic);

    void createTopic(String topicName, String description);

    void removeUserFromTopic(Long userId, String topic);

    public List<TopicResponse> getAllTopics();
}