package com.project.libmanager.controller.user;

import com.project.libmanager.service.IPrivateMessageService;
import com.project.libmanager.service.dto.request.PrivateMessageRequest;
import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.PrivateMessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Chat Management", description = "Send messages to chat rooms")
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
public class ChatController {
    private final IPrivateMessageService privateMessageService;

    @Operation(summary = "Get private messages between two users")
    @GetMapping("/private/messages")
    public ResponseEntity<ApiResponse<List<PrivateMessageResponse>>> getPrivateMessages(
            @RequestParam Long senderId,
            @RequestParam Long receiverId
    ) {
        List<PrivateMessageResponse> messages = privateMessageService.getMessagesBetweenUsers(senderId, receiverId);
        ApiResponse<List<PrivateMessageResponse>> response = ApiResponse.<List<PrivateMessageResponse>>builder()
                .message("Messages retrieved successfully")
                .result(messages)
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete private message by ID")
    @DeleteMapping("/private/message/{id}")
    public ResponseEntity<ApiResponse<String>> deletePrivateMessage(@PathVariable String id) {
        privateMessageService.deleteMessage(id);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Message deleted successfully")
                .result(id)
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Send private message", description = "Send a private message from one user to another")
    @PostMapping("/private/send")
    public ResponseEntity<ApiResponse<PrivateMessageResponse>> sendPrivateMessage(
            @Valid @RequestBody PrivateMessageRequest privateMessageRequest
    ) {
        PrivateMessageResponse responseMessage = privateMessageService.sendMessagePrivate(privateMessageRequest);
        ApiResponse<PrivateMessageResponse> response = ApiResponse.<PrivateMessageResponse>builder()
                .message("Private message sent successfully")
                .result(responseMessage)
                .build();
        return ResponseEntity.ok(response);
    }

}
