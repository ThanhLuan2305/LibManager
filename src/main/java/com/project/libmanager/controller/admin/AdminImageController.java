package com.project.libmanager.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.IImageCloundService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.io.IOException;

@RestController
@RequestMapping("admin/images")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "JWT Authentication")
public class AdminImageController {

    private final IImageCloundService imageCloudService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam("file") MultipartFile file)
            throws IOException {
        String imageUrl = imageCloudService.uploadImage(file);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message("Image uploaded successfully")
                .result(imageUrl)
                .build());
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<ApiResponse<String>> deleteImage(@PathVariable String fileName) {
        imageCloudService.deleteImage(fileName);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message("Image deleted successfully")
                .result(fileName)
                .build());
    }

    @PutMapping
    public ResponseEntity<ApiResponse<String>> updateImage(
            @RequestParam("oldFileName") String oldFileName,
            @RequestParam("file") MultipartFile newFile) {
        String newImageUrl = imageCloudService.updateImage(oldFileName, newFile);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message("Image updated successfully")
                .result(newImageUrl)
                .build());
    }

    @GetMapping("/preview/{fileName}")
    public ResponseEntity<ApiResponse<String>> getPreviewUrl(@PathVariable String fileName) {
        String imageUrl = imageCloudService.getPreviewUrl(fileName);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message("Image preview URL retrieved successfully")
                .result(imageUrl)
                .build());
    }

}
