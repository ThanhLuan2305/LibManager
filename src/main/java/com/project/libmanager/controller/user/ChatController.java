package com.project.libmanager.controller.user;

import com.project.libmanager.exception.AppException;
import com.project.libmanager.service.IPrivateMessageService;
import com.project.libmanager.service.dto.request.PrivateMessageRequest;
import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.PrivateMessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing chat-related operations.
 * Provides endpoints for sending, retrieving, and deleting private messages.
 */
@Tag(name = "Chat Management", description = "Endpoints for managing private messages between users")
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT Authentication")
public class ChatController {
    private final IPrivateMessageService privateMessageService; // Service for private message operations

    /**
     * Retrieves private messages exchanged between two users.
     *
     * @param senderId   the ID of the user who sent the messages
     * @param receiverId the ID of the user who received the messages
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a list of {@link PrivateMessageResponse} objects
     * @implNote Delegates to {@link IPrivateMessageService} to fetch messages and wraps them in an {@link ApiResponse}.
     */
    @Operation(summary = "Get private messages between two users",
            description = "Retrieves all private messages exchanged between the specified sender and receiver.")
    @GetMapping("/private/messages")
    public ResponseEntity<ApiResponse<List<PrivateMessageResponse>>> getPrivateMessages(
            @Parameter(name = "senderId", description = "ID of the sender", example = "1") @RequestParam Long senderId,
            @Parameter(name = "receiverId", description = "ID of the receiver", example = "2") @RequestParam Long receiverId
    ) {
        List<PrivateMessageResponse> messages = privateMessageService.getMessagesBetweenUsers(senderId, receiverId);
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
     * @implNote Delegates to {@link IPrivateMessageService} to send the message and returns it in an {@link ApiResponse}.
     */
    @Operation(summary = "Send private message",
            description = "Sends a private message from one user to another and returns the message details.")
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