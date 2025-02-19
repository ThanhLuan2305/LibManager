package com.project.LibManager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.project.LibManager.service.IImageCloundService;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class ImageControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IImageCloundService iImageCloundService;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUploadImage_Success() throws Exception {
        MultipartFile mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
        String expectedImageUrl = "http://example.com/test.jpg";

        when(iImageCloundService.uploadImage(any(MultipartFile.class))).thenReturn(expectedImageUrl);

        mockMvc.perform(multipart("/images/admin/upload")
                        .file("file", mockFile.getBytes())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value(expectedImageUrl));

        verify(iImageCloundService, times(1)).uploadImage(any(MultipartFile.class));
    }

    @Test   
    @WithMockUser(roles = "ADMIN")
    public void testUploadImage_Failure() throws Exception {
        MultipartFile mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
        String errorMessage = "Upload failed";

        when(iImageCloundService.uploadImage(any(MultipartFile.class))).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(multipart("/images/admin/upload")
                        .file("file", mockFile.getBytes())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));

        verify(iImageCloundService, times(1)).uploadImage(any(MultipartFile.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteImage_Success() throws Exception {
        String fileName = "test-image.jpg";
        when(iImageCloundService.deleteImage(fileName)).thenReturn(true);

        mockMvc.perform(delete("/images/admin/" + fileName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Delete successfully!"));

        verify(iImageCloundService, times(1)).deleteImage(fileName);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteImage_NonExistentFile() throws Exception {
        String nonExistentFileName = "non-existent-image.jpg";
        when(iImageCloundService.deleteImage(nonExistentFileName)).thenReturn(false);

        mockMvc.perform(delete("/images/admin/" + nonExistentFileName))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Can't delete image"));

        verify(iImageCloundService, times(1)).deleteImage(nonExistentFileName);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateImage_Success() throws Exception {
        String oldFileName = "old-image.jpg";
        MockMultipartFile mockFile = new MockMultipartFile("file", "new-image.jpg", "image/jpeg", "new image content".getBytes());
        String expectedNewImageUrl = "http://example.com/new-image.jpg";

        when(iImageCloundService.updateImage(eq(oldFileName), any(MultipartFile.class))).thenReturn(expectedNewImageUrl);

        mockMvc.perform(multipart("/images/admin/update")
                        .file(mockFile)
                        .param("oldFileName", oldFileName)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newImageUrl").value(expectedNewImageUrl));

        verify(iImageCloundService, times(1)).updateImage(eq(oldFileName), any(MultipartFile.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateImage_OldFileNameNotFound() throws Exception {
        String oldFileName = "non-existent-image.jpg";
        MockMultipartFile mockFile = new MockMultipartFile("file", "new-image.jpg", "image/jpeg", "new image content".getBytes());
        String errorMessage = "Old file not found";

        when(iImageCloundService.updateImage(eq(oldFileName), any(MultipartFile.class))).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(multipart("/images/admin/update")
                        .file(mockFile)
                        .param("oldFileName", oldFileName)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));

        verify(iImageCloundService, times(1)).updateImage(eq(oldFileName), any(MultipartFile.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetPreviewUrl_NonExistentFile() throws Exception {
        String nonExistentFileName = "non-existent-image.jpg";
        String expectedPreviewUrl = "http://example.com/preview/non-existent-image.jpg";

        when(iImageCloundService.getPreviewUrl(nonExistentFileName)).thenReturn(expectedPreviewUrl);

        mockMvc.perform(get("/images/admin/preview/" + nonExistentFileName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.previewUrl").value(expectedPreviewUrl));

        verify(iImageCloundService, times(1)).getPreviewUrl(nonExistentFileName);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetPreviewUrl_Success() throws Exception {
        String fileName = "test-image.jpg";
        String expectedPreviewUrl = "http://example.com/preview/test-image.jpg";

        when(iImageCloundService.getPreviewUrl(fileName)).thenReturn(expectedPreviewUrl);

        mockMvc.perform(get("/images/admin/preview/" + fileName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.previewUrl").value(expectedPreviewUrl));

        verify(iImageCloundService, times(1)).getPreviewUrl(fileName);
    }
}