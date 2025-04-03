package com.project.libmanager.controller.admin;

import com.project.libmanager.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.IImageCloundService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;

/**
 * REST controller for managing image operations by admin users.
 * Provides endpoints for uploading, deleting, updating, and retrieving image preview URLs.
 */
@RestController
@RequestMapping("admin/images")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "JWT Authentication")
@Tag(name = "Admin Image Management", description = "Endpoints for managing images by admin users")
public class AdminImageController {

    private final IImageCloundService imageCloudService;

    /**
     * Uploads an image to the cloud storage.
     *
     * @param file the image file to upload
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with the URL of the uploaded image
     * @throws IOException  if:
     *                      - file upload fails (ErrorCode.FILE_UPLOAD_FAILED)
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - user not authorized (ErrorCode.UNAUTHORIZED)
     * @implNote Delegates to {@link IImageCloundService} to upload the image and returns the URL in an {@link ApiResponse}.
     */
    @PostMapping("/upload")
    @Operation(summary = "Upload an image",
            description = "Uploads an image to the cloud storage.")
    @Parameter(name = "file", description = "Image file to upload")
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam("file") MultipartFile file)
            throws IOException {
        String imageUrl = imageCloudService.uploadImage(file);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message("Image uploaded successfully")
                .result(imageUrl)
                .build());
    }

    /**
     * Deletes an image from the cloud storage by its file name.
     *
     * @param fileName the name of the image file to delete
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with the deleted file name
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - user not authorized (ErrorCode.UNAUTHORIZED)
     *                      - image not found (ErrorCode.IMAGE_NOT_FOUND)
     * @implNote Delegates to {@link IImageCloundService} to delete the image and returns the file name in an {@link ApiResponse}.
     */
    @DeleteMapping("/{fileName}")
    @Operation(summary = "Delete an image",
            description = "Deletes an image from the cloud storage by its file name.")
    @Parameter(description = "Name of the image file to delete")
    public ResponseEntity<ApiResponse<String>> deleteImage(@PathVariable String fileName) {
        imageCloudService.deleteImage(fileName);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message("Image deleted successfully")
                .result(fileName)
                .build());
    }

    /**
     * Updates an existing image in the cloud storage with a new file.
     *
     * @param oldFileName the name of the existing image file to update
     * @param newFile     the new image file to upload
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with the URL of the updated image
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - user not authorized (ErrorCode.UNAUTHORIZED)
     *                      - image not found (ErrorCode.IMAGE_NOT_FOUND)
     * @implNote Delegates to {@link IImageCloundService} to update the image and returns the new URL in an {@link ApiResponse}.
     */
    @PutMapping
    @Operation(summary = "Update an image",
            description = "Updates an existing image in the cloud storage with a new file.")
    @Parameter(name = "oldFileName", description = "Name of the existing image file to update")
    @Parameter(name = "file", description = "New image file to upload")
    public ResponseEntity<ApiResponse<String>> updateImage(
            @RequestParam("oldFileName") String oldFileName,
            @RequestParam("file") MultipartFile newFile) {
        String newImageUrl = imageCloudService.updateImage(oldFileName, newFile);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message("Image updated successfully")
                .result(newImageUrl)
                .build());
    }

    /**
     * Retrieves the preview URL of an image by its file name.
     *
     * @param fileName the name of the image file to retrieve
     * @return a {@link ResponseEntity} containing:
     * - an {@link ApiResponse} with the preview URL of the image
     * @throws AppException if:
     *                      - user not authenticated (ErrorCode.UNAUTHENTICATED)
     *                      - user not authorized (ErrorCode.UNAUTHORIZED)
     *                      - image not found (ErrorCode.IMAGE_NOT_FOUND)
     * @implNote Delegates to {@link IImageCloundService} to get the preview URL and returns it in an {@link ApiResponse}.
     */
    @GetMapping("/preview/{fileName}")
    @Operation(summary = "Get image preview URL",
            description = "Retrieves the preview URL of an image by its file name.")
    @Parameter(description = "Name of the image file to retrieve")
    public ResponseEntity<ApiResponse<String>> getPreviewUrl(@PathVariable String fileName) {
        String imageUrl = imageCloudService.getPreviewUrl(fileName);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message("Image preview URL retrieved successfully")
                .result(imageUrl)
                .build());
    }
}