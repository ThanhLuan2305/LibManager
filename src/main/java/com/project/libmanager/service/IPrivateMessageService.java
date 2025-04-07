package com.project.libmanager.service;

import com.project.libmanager.service.dto.request.PrivateMessageRequest;
import com.project.libmanager.service.dto.response.PrivateMessageResponse;

import java.util.List;
import java.util.Map;

public interface IPrivateMessageService {
    List<PrivateMessageResponse> getMessagesBetweenUsers(Long user1, Long user2);

    PrivateMessageResponse sendMessagePrivate(PrivateMessageRequest request);

    Map<Long, List<PrivateMessageResponse>> getMessAdminWithAllUser();

    void deleteMessage(String messageId);
}
