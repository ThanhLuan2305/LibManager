package com.project.LibManager.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.project.LibManager.dto.response.ApiResponse;
import com.project.LibManager.service.IImageCloundService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("admin/images")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "JWT Authentication")
public class AdminImageController {

    private final IImageCloundService imageCloudService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String imageUrl = imageCloudService.uploadImage(file);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message("Image uploaded successfully")
                .result(imageUrl)
                .build());
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<ApiResponse<String>> deleteImage(@PathVariable String fileName) throws Exception {
        imageCloudService.deleteImage(fileName);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message("Image deleted successfully")
                .result(fileName)
                .build());
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<String>> updateImage(
            @RequestParam("oldFileName") String oldFileName,
            @RequestParam("file") MultipartFile newFile) throws Exception {
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

