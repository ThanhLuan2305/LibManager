package com.project.libmanager.controller.admin;

import com.project.libmanager.exception.AppException;
import com.project.libmanager.service.IPrivateMessageService;
import com.project.libmanager.service.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private final IPrivateMessageService privateMessageService; // Service for private message operations

    /**
     * Retrieves all private messages between the admin and all users.
     *
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with a map of user IDs to their message lists
     * @throws AppException if the authenticated admin user cannot be retrieved
     * @implNote Delegates to {@link IPrivateMessageService} to fetch messages and wraps the result in an {@link ApiResponse}.
     */
    @Operation(summary = "Get all private messages between admin and all users",
            description = "Retrieves all private messages between the authenticated admin and all non-admin users.")
    @GetMapping("/private/admin/all")
    public ResponseEntity<ApiResponse<Object>> getAllMessagesWithUsers() {
        var result = privateMessageService.getMessAdminWithAllUser();
        ApiResponse<Object> response = ApiResponse.builder()
                .message("Fetched all messages between admin and users")
                .result(result)
                .build();
        return ResponseEntity.ok(response);
    }
}