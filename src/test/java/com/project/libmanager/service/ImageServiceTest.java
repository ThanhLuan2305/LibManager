//package com.project.libmanager.service;
//
//import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyBoolean;
//import static org.mockito.ArgumentMatchers.anyMap;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.ArgumentMatchers.argThat;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.atLeastOnce;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.doReturn;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.spy;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.logging.Logger;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.springframework.web.multipart.MultipartFile;
//
//import com.cloudinary.Api;
//import com.cloudinary.Cloudinary;
//import com.cloudinary.Uploader;
//import com.cloudinary.utils.ObjectUtils;
//import com.project.libmanager.config.CloudinaryConfig;
//import com.project.libmanager.constant.ErrorCode;
//import com.project.libmanager.dto.response.ApiResponse;
//import com.project.libmanager.exception.AppException;
//import com.project.libmanager.service.impl.ImageCloundServiceImpl;
//import ch.qos.logback.classic.spi.ILoggingEvent;
//import ch.qos.logback.core.Appender;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@SpringBootTest
//@TestPropertySource("/test.properties")
//public class ImageServiceTest {
//    @InjectMocks
//    private ImageCloundServiceImpl imageService;
//
//    @Mock
//    private Cloudinary cloudinary;
//
//    @Mock
//    private MultipartFile mockFile;
//
//    @Mock
//    private Api cloudinaryApi;
//
//    @Mock
//    private Uploader uploader;
//
//    private Logger mockLogger = mock(Logger.class);
//
//    private final String testUrl = "https://res.cloudinary.com/test/image/upload/sample.jpg";
//    private final String folder = "test_folder";
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        imageService = new ImageCloundServiceImpl(cloudinary);
//        when(cloudinary.api()).thenReturn(cloudinaryApi);
//    }
//
//    @Test
//    void uploadImage_SuccessfulUpload() throws Exception {
//        // Arrange
//        String expectedSecureUrl = "https://res.cloudinary.com/test/image/upload/v1234567890/test_folder/test_image.jpg";
//        MultipartFile mockFile = mock(MultipartFile.class);
//        when(mockFile.getOriginalFilename()).thenReturn("test_image.jpg");
//        when(mockFile.getBytes()).thenReturn(new byte[]{1, 2, 3});
//        when(mockFile.getSize()).thenReturn(1024L);
//
//        ImageCloundServiceImpl spyImageService = spy(imageService);
//        doNothing().when(spyImageService).validateFile(mockFile);
//
//        Uploader uploader = mock(Uploader.class);
//        when(cloudinary.uploader()).thenReturn(uploader);
//
//        Map<String, Object> uploadResult = new HashMap<>();
//        uploadResult.put("secure_url", expectedSecureUrl);
//        when(uploader.upload(any(File.class), any(Map.class))).thenReturn(uploadResult);
//
//        // Act
//        String actualSecureUrl = spyImageService.uploadImage(mockFile);
//
//        // Assert
//        assertEquals(expectedSecureUrl, actualSecureUrl);
//        verify(cloudinary.uploader(), atLeastOnce()).upload(any(File.class), any(Map.class));
//        verify(spyImageService).validateFile(mockFile);
//    }
//
//    @Test
//    void uploadImage_FileTooLarge_ThrowsException() {
//        when(mockFile.getSize()).thenReturn(6 * 1024 * 1024L); // Giả lập file > 5MB
//        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
//
//        Exception exception = assertThrows(AppException.class, () -> {
//            imageService.uploadImage(mockFile);
//        });
//
//        assertEquals("Uncategorized error", exception.getMessage());
//    }
//
//    @Test
//    void uploadImage_CloudinaryUploadFails_ThrowsException() throws Exception {
//        MultipartFile mockFile = mock(MultipartFile.class);
//        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
//        when(mockFile.getBytes()).thenReturn(new byte[]{1, 2, 3});
//        when(mockFile.getSize()).thenReturn(1024L);
//
//        ImageCloundServiceImpl spyImageService = spy(imageService);
//        doNothing().when(spyImageService).validateFile(mockFile);
//
//        Uploader uploader = mock(Uploader.class);
//        when(cloudinary.uploader()).thenReturn(uploader);
//        when(uploader.upload(any(File.class), any(Map.class))).thenThrow(new IOException("Cloudinary Error"));
//
//        Exception exception = assertThrows(AppException.class, () -> {
//            spyImageService.uploadImage(mockFile);
//        });
//
//        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION, ((AppException) exception).getErrorCode());
//    }
//
//
//    @Test
//    void uploadImage_ThrowsException() throws Exception {
//        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
//        when(mockFile.getBytes()).thenThrow(new IOException("Test Error"));
//
//        assertThrows(AppException.class, () -> imageService.uploadImage(mockFile));
//    }
//
//    @Test
//    void deleteImage_Success() throws Exception {
//        String fileName = "test_image.jpg";
//        String publicId = null + "/" + fileName;
//
//        com.cloudinary.api.ApiResponse resourceResponse = mock(com.cloudinary.api.ApiResponse.class);
//
//
//        when(cloudinary.uploader()).thenReturn(uploader);
//        when(resourceResponse.get(anyString())).thenReturn(new HashMap<>());
//        when(cloudinaryApi.resource(publicId, ObjectUtils.emptyMap())).thenReturn(resourceResponse);
//
//
//        Map<String, Object> deleteResponse = new HashMap<>();
//        deleteResponse.put("result", "ok");
//        when(cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())).thenReturn(deleteResponse);
//
//        assertTrue(imageService.deleteImage(fileName));
//    }
//
//    @Test
//    void deleteImage_FileNotFound_ThrowsAppException() throws Exception {
//        String fileName = "not_exist.jpg";
//        String publicId = null + "/" + fileName;
//
//        when(cloudinaryApi.resource(publicId, ObjectUtils.emptyMap()))
//            .thenThrow(new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
//
//        AppException exception = assertThrows(AppException.class, () -> imageService.deleteImage(fileName));
//        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION, exception.getErrorCode());
//    }
//
//    @Test
//    void deleteImage_ThrowsAppException() throws Exception {
//        String fileName = "error.jpg";
//        String publicId = null + "/" + fileName;
//
//        when(cloudinaryApi.resource(publicId, ObjectUtils.emptyMap())).thenThrow(new RuntimeException("API Error"));
//
//        AppException exception = assertThrows(AppException.class, () -> imageService.deleteImage(fileName));
//        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION, exception.getErrorCode());
//    }
//
//    @Test
//    void deleteImage_ImageDoesNotExist_ReturnsFalse() throws Exception {
//        // Arrange
//        String fileName = "non_existent_image.jpg";
//        String publicId = null + "/" + fileName;
//
//        when(cloudinary.api()).thenReturn(cloudinaryApi);
//
//        when(cloudinaryApi.resource(publicId, ObjectUtils.emptyMap())).thenReturn(null);
//        boolean result = imageService.deleteImage(fileName);
//
//         // Assert
//        assertFalse(result);
//        verify(cloudinaryApi, times(2)).resource(eq(publicId), eq(ObjectUtils.emptyMap()));
//    }
//
//    @Test
//    void updateImage_OldImageDeletionFails_NewImageUploadSucceeds() throws Exception {
//        // Arrange
//        String oldFileName = "old_image.jpg";
//        MultipartFile newFile = mock(MultipartFile.class);
//        String expectedNewUrl = "https://res.cloudinary.com/test/image/upload/v1234567890/test_folder/new_image.jpg";
//
//        ImageCloundServiceImpl spyImageService = spy(imageService);
//        doReturn(false).when(spyImageService).deleteImage(oldFileName);
//        doReturn(expectedNewUrl).when(spyImageService).uploadImage(newFile);
//
//        // Act
//        String actualNewUrl = spyImageService.updateImage(oldFileName, newFile);
//
//        // Assert
//        assertEquals(expectedNewUrl, actualNewUrl);
//        verify(spyImageService).deleteImage(oldFileName);
//        verify(spyImageService).uploadImage(newFile);
//    }
//
//    @Test
//    void updateImage_OldImageDeletionSucceeds_NewImageUploadFails_ThrowsAppException() throws Exception {
//        // Arrange
//        String oldFileName = "old_image.jpg";
//        MultipartFile newFile = mock(MultipartFile.class);
//
//        ImageCloundServiceImpl spyImageService = spy(imageService);
//        doReturn(true).when(spyImageService).deleteImage(oldFileName);
//        doThrow(new RuntimeException("Upload failed")).when(spyImageService).uploadImage(newFile);
//
//        // Act & Assert
//        AppException exception = assertThrows(AppException.class, () -> spyImageService.updateImage(oldFileName, newFile));
//        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION, exception.getErrorCode());
//
//        verify(spyImageService).deleteImage(oldFileName);
//        verify(spyImageService).uploadImage(newFile);
//    }
//
//    @Test
//    void updateImage_LogsWarningWhenOldImageDeletionFails() throws Exception {
//        // Arrange
//        String oldFileName = "old_image.jpg";
//        MultipartFile newFile = mock(MultipartFile.class);
//        String expectedNewUrl = "https://res.cloudinary.com/test/image/upload/v1234567890/test_folder/new_image.jpg";
//
//        ImageCloundServiceImpl spyImageService = spy(imageService);
//        doReturn(false).when(spyImageService).deleteImage(oldFileName);
//        doReturn(expectedNewUrl).when(spyImageService).uploadImage(newFile);
//
//        // Act
//        String actualNewUrl = spyImageService.updateImage(oldFileName, newFile);
//
//        // Assert
//        assertEquals(expectedNewUrl, actualNewUrl);
//        verify(spyImageService).deleteImage(oldFileName);
//        verify(spyImageService).uploadImage(newFile);
//    }
//
//    @Test
//    void updateImage_NullOrEmptyOldFileName_ShouldUploadNewImage() throws Exception {
//        // Arrange
//        String oldFileName = "";  // Test with empty string, can also use null
//        MultipartFile newFile = mock(MultipartFile.class);
//        String expectedNewUrl = "https://res.cloudinary.com/test/image/upload/v1234567890/test_folder/new_image.jpg";
//
//        ImageCloundServiceImpl spyImageService = spy(imageService);
//        doReturn(false).when(spyImageService).deleteImage(anyString());
//        doReturn(expectedNewUrl).when(spyImageService).uploadImage(newFile);
//
//        // Act
//        String actualNewUrl = spyImageService.updateImage(oldFileName, newFile);
//
//        // Assert
//        assertEquals(expectedNewUrl, actualNewUrl);
//        verify(spyImageService).deleteImage(anyString());
//        verify(spyImageService).uploadImage(newFile);
//    }
//
//    @Test
//    void getPreviewUrl_FileDoesNotExist_ThrowsIllegalArgumentException() throws Exception {
//        // Arrange
//        String fileName = "non_existent_file.jpg";
//        String fullPath = "null/" + fileName;  // Modified to use "null/" instead of folder
//
//        when(cloudinary.api()).thenReturn(cloudinaryApi);
//        when(cloudinaryApi.resource(eq(fullPath), eq(ObjectUtils.emptyMap())))
//                .thenThrow(new RuntimeException("File not found"));
//
//        // Act & Assert
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
//                () -> imageService.getPreviewUrl(fileName));
//        assertEquals("File does not exist on Cloudinary.", exception.getMessage());
//
//        verify(cloudinary.api()).resource(eq(fullPath), eq(ObjectUtils.emptyMap()));
//    }
//
//    @Test
//void getPreviewUrl_Success() throws Exception {
//    // Arrange
//    String fileName = "test_image.jpg";
//    String fullPath = null + "/" + fileName;
//    String expectedUrl = "https://res.cloudinary.com/test-cloud/image/upload/" + fullPath;
//    com.cloudinary.api.ApiResponse resourceResponse = mock(com.cloudinary.api.ApiResponse.class);
//
//    when(cloudinary.api()).thenReturn(cloudinaryApi);
//    when(cloudinaryApi.resource(eq(fullPath), eq(ObjectUtils.emptyMap()))).thenReturn(resourceResponse);
//
//    // Mock Cloudinary configuration
//    com.cloudinary.Configuration mockConfig = mock(com.cloudinary.Configuration.class);
//    mockConfig.cloudName = "test-cloud";
//    ReflectionTestUtils.setField(cloudinary, "config", mockConfig);
//
//    // Act
//    String actualUrl = imageService.getPreviewUrl(fileName);
//
//    // Assert
//    assertEquals(expectedUrl, actualUrl);
//    verify(cloudinaryApi).resource(eq(fullPath), eq(ObjectUtils.emptyMap()));
//}
//
//    @Test
//    void validateFile_FileSizeExceedsMaxLimit_ThrowsIllegalArgumentException() {
//        // Arrange
//        long maxFileSize = 5 * 1024 * 1024; // 5MB in bytes
//        MultipartFile mockFile = mock(MultipartFile.class);
//        when(mockFile.getSize()).thenReturn(maxFileSize + 1); // Exceed the limit by 1 byte
//        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
//
//        // Set the maxFileSize and allowedExtensions fields in the imageService
//        ReflectionTestUtils.setField(imageService, "maxFileSize", maxFileSize);
//        ReflectionTestUtils.setField(imageService, "allowedExtensions", "jpg,jpeg,png,gif");
//
//        // Act & Assert
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
//                () -> imageService.validateFile(mockFile));
//        assertEquals("File exceeds the allowed size limit (5MB).", exception.getMessage());
//    }
//
//    @Test
//    void validateFile_FileNameWithoutExtension_ThrowsIllegalArgumentException() {
//        // Arrange
//        MultipartFile mockFile = mock(MultipartFile.class);
//        when(mockFile.getOriginalFilename()).thenReturn("invalidfilename");
//        when(mockFile.getSize()).thenReturn(1L); // Set a size smaller than maxFileSize
//
//        // Set the maxFileSize field in the imageService
//        ReflectionTestUtils.setField(imageService, "maxFileSize", 5 * 1024 * 1024L); // 5MB
//
//        // Act & Assert
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
//                () -> imageService.validateFile(mockFile));
//        assertEquals("Invalid file.", exception.getMessage());
//    }
//
//    @Test
//    void validateFile_InvalidFileFormat_ThrowsIllegalArgumentException() {
//        // Arrange
//        MultipartFile mockFile = mock(MultipartFile.class);
//        when(mockFile.getOriginalFilename()).thenReturn("test.pdf");
//        when(mockFile.getSize()).thenReturn(1024L); // Set a size smaller than maxFileSize
//
//        // Set the maxFileSize and allowedExtensions fields in the imageService
//        ReflectionTestUtils.setField(imageService, "maxFileSize", 5 * 1024 * 1024L); // 5MB
//        ReflectionTestUtils.setField(imageService, "allowedExtensions", "jpg,jpeg,png,gif");
//
//        // Act & Assert
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
//                () -> imageService.validateFile(mockFile));
//        assertEquals("Invalid file format. Only JPG, JPEG, PNG, and GIF are allowed.", exception.getMessage());
//    }
//
//    @Test
//    void validateFile_SuccessfulValidation() {
//        // Arrange
//        MultipartFile mockFile = mock(MultipartFile.class);
//        when(mockFile.getSize()).thenReturn(1024L); // 1KB
//        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
//
//        ReflectionTestUtils.setField(imageService, "maxFileSize", 5 * 1024 * 1024L); // 5MB
//        ReflectionTestUtils.setField(imageService, "allowedExtensions", "jpg,jpeg,png,gif");
//
//        // Act & Assert
//        assertDoesNotThrow(() -> imageService.validateFile(mockFile));
//    }
//
//    @Test
//    void validateFile_NullFileName() {
//        when(mockFile.getOriginalFilename()).thenReturn(null);
//
//        assertThrows(IllegalArgumentException.class, () -> imageService.validateFile(mockFile));
//    }
//}
