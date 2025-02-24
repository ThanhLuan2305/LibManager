package com.project.LibManager.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.project.LibManager.service.IImageCloundService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.Map;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "JWT Authentication")
public class ImageController {

    private final IImageCloundService imageCloudService;

    @PostMapping("/admin/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = imageCloudService.uploadImage(file);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (Exception e) {
            log.error("Lỗi upload ảnh: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/admin/{fileName}")
    public ResponseEntity<Map<String, String>> deleteImage(@PathVariable String fileName) {
        boolean isDeleted = imageCloudService.deleteImage(fileName);
        if (isDeleted) {
            return ResponseEntity.ok(Map.of("message", "Delete successfully!"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Can't delete image"));
        }
    }

    @PutMapping("/admin/update")
    public ResponseEntity<Map<String, String>> updateImage(
            @RequestParam("oldFileName") String oldFileName,
            @RequestParam("file") MultipartFile newFile) {
        try {
            String newImageUrl = imageCloudService.updateImage(oldFileName, newFile);
            return ResponseEntity.ok(Map.of("newImageUrl", newImageUrl));
        } catch (Exception e) {
            log.error("Error update image: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin/preview/{fileName}")
    public ResponseEntity<Map<String, String>> getPreviewUrl(@PathVariable String fileName) {
        String imageUrl = imageCloudService.getPreviewUrl(fileName);
        return ResponseEntity.ok(Map.of("previewUrl", imageUrl));
    }
}

