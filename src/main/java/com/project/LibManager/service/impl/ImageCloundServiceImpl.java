package com.project.LibManager.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.project.LibManager.constant.ErrorCode;
import com.project.LibManager.exception.AppException;
import com.project.LibManager.service.IImageCloundService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageCloundServiceImpl implements IImageCloundService{
    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder}")
    private String folder;

    @Value("${cloudinary.max_file_size}")
    private long maxFileSize;

    @Value("${cloudinary.allowed_extensions}")
    private String allowedExtensions;

    /**
     * Uploads an image to Cloudinary.
     * 
     * @param imgUrl The image file to be uploaded.
     * @return The secure URL of the uploaded image.
     * @throws AppException If the image cannot be uploaded or an error occurs.
     * @implNote This method validates the file before uploading and uses Cloudinary API to upload the image.
     *           It stores the image in a specified folder and generates a secure URL.
     */
    @Override
    public String uploadImage(MultipartFile imgUrl) {
        try {
            validateFile(imgUrl);

            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + imgUrl.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(convFile)) {
                fos.write(imgUrl.getBytes());
            }
    
            Map<String, Object> params1 = ObjectUtils.asMap(
                    "use_filename", true,
                    "unique_filename", false,
                    "overwrite", true,
                    "folder", folder
            );
    
            var pic = cloudinary.uploader().upload(convFile, params1);
    
            return (String) pic.get("secure_url");
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Deletes an image from Cloudinary.
     * 
     * @param fileName The name of the image to be deleted.
     * @return true if the image was successfully deleted, false if the image was not found.
     * @throws AppException If the deletion process encounters an error.
     * @implNote This method attempts to delete an image by its public ID on Cloudinary. It logs warnings if the image 
     *           is not found or if there are issues with deletion.
     */
    @Override
    public boolean deleteImage(String fileName) {
        try {
            String publicId = folder + "/" + fileName;
    
            try {
                cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
            } catch (Exception e) {
                log.warn("Image '{}' does not exist on Cloudinary.", publicId);
                return false;
            }
    
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String resultStatus = (String) result.get("result");
    
            if ("not found".equals(resultStatus)) {
                log.warn("Image '{}' was not found in Cloudinary.", publicId);
                return false;
            }
    
            log.info("Image '{}' deleted successfully from Cloudinary.", publicId);
            return true;
        } catch (Exception e) {
            log.error("Error deleting image '{}': {}", fileName, e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Updates an existing image by deleting the old image and uploading the new one.
     * 
     * @param oldFileName The name of the old image file.
     * @param newFile The new image file to be uploaded.
     * @return The secure URL of the new image.
     * @throws IOException If the update process encounters an error.
     * @implNote This method first deletes the old image (if it exists) and then uploads the new image to Cloudinary.
     */
    @Override
    public String updateImage(String oldFileName, MultipartFile newFile) throws IOException {
        try {
            if (!deleteImage(oldFileName)) {
                log.warn("Failed to delete old image '{}'. Proceeding with upload of new image.", oldFileName);
            }
        
            return uploadImage(newFile);
        } catch (Exception e) {
            log.error("Error update image: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            
        }
    }

    /**
     * Retrieves the preview URL of an image from Cloudinary.
     * 
     * @param fileName The name of the image to retrieve.
     * @return The URL for previewing the image.
     * @throws IllegalArgumentException If the file does not exist on Cloudinary.
     * @implNote This method constructs a URL for the image preview by using Cloudinary's CDN.
     */
    @Override
    public String getPreviewUrl(String fileName) {
        try {
            Map result = cloudinary.api().resource(folder + "/" + fileName, ObjectUtils.emptyMap());
    
            return "https://res.cloudinary.com/" + cloudinary.config.cloudName + "/image/upload/" + folder + "/" + fileName;
        } catch (Exception e) {
            throw new IllegalArgumentException("File does not exist on Cloudinary.");
        }
    }

    /**
     * Validates an image file before uploading.
     * 
     * @param file The image file to be validated.
     * @throws IllegalArgumentException If the file exceeds the size limit or has an invalid extension.
     * @implNote This method checks if the file size is within the allowed limit and if the file extension is allowed.
     */
    @Override
    public void validateFile(MultipartFile file) {
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File exceeds the allowed size limit (5MB).");
        }
    
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("Invalid file.");
        }
        
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
        String[] allowedExtensionsArray = allowedExtensions.split(",");
        if (!Arrays.asList(allowedExtensionsArray).contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file format. Only JPG, JPEG, PNG, and GIF are allowed.");
        }
    }   
}
