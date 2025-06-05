package com.project.libmanager.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.service.IImageCloundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@link IImageCloundService} for managing image operations on Cloudinary.
 * Handles upload, deletion, update, and preview URL retrieval of images.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImageCloundServiceImpl implements IImageCloundService {
    private final Cloudinary cloudinary;            // Cloudinary client for image operations

    @Value("${cloudinary.folder}")
    private String folder;                          // Cloudinary folder path for organizing images

    @Value("${cloudinary.max_file_size}")
    private long maxFileSize;                       // Maximum allowed file size (in bytes) from config

    @Value("${cloudinary.allowed_extensions}")
    private String allowedExtensions;               // Comma-separated list of allowed file extensions

    private static final String NO_FILE = "File does not exist on Cloudinary."; // Constant for not-found error message

    /**
     * Uploads an image to Cloudinary and returns its secure URL.
     *
     * @param imgUrl the {@link MultipartFile} image to upload
     * @return the secure URL of the uploaded image on Cloudinary
     * @throws IOException  if file I/O operations fail
     * @throws AppException if validation or upload fails (wrapped as IOException or Exception)
     * @implNote Validates the file, converts it to a temporary file, and uploads it to Cloudinary
     * with specific parameters (e.g., folder, overwrite).
     */
    @Override
    public String uploadImage(MultipartFile imgUrl) throws IOException {
        try {
            // Validate file size and extension before processing
            validateFile(imgUrl);

            // Create temp file in system temp directory; uses original filename for simplicity
            File convFile = new File(
                    System.getProperty("java.io.tmpdir") + File.separator + imgUrl.getOriginalFilename());
            // Write MultipartFile bytes to temp file; auto-closes stream with try-with-resources
            try (FileOutputStream fos = new FileOutputStream(convFile)) {
                fos.write(imgUrl.getBytes());
            }

            // Define upload parameters; preserves filename, allows to overwrite in specified folder
            Map<String, Object> params1 = Map.of(
                    "use_filename", true,       // Use original filename as public ID
                    "unique_filename", false,   // Avoid appending unique suffix
                    "overwrite", true,          // Replace existing file with same public ID
                    "folder", folder);          // Store in configured folder

            // Upload file to Cloudinary and extract secure URL; assumes successful response
            return (String) cloudinary.uploader().upload(convFile, params1).get("secure_url");
        } catch (IOException e) {
            // Log I/O error and rethrow for caller to handle
            log.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            // Log unexpected errors and rethrow as-is
            log.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Deletes an image from Cloudinary by its filename.
     *
     * @param fileName the name of the image file (without folder prefix)
     * @return {@code true} if deletion succeeds, {@code false} if image not found
     * @throws Exception if deletion fails due to I/O or unexpected errors
     * @implNote Constructs public ID from folder and filename, checks Cloudinary response,
     * and logs outcome.
     */
    @Override
    public boolean deleteImage(String fileName) {
        // Construct public ID; combines folder and filename for Cloudinary
        String publicId = folder + "/" + fileName;
        try {
            // Delete image; empty options map uses default settings
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

            // Extract result status from response
            String resultStatus = (String) result.get("result");

            // Handle not-found case; Cloudinary returns "not found" if image doesn't exist
            if ("not found".equals(resultStatus)) {
                throw new IllegalArgumentException("Image '" + publicId + "' does not exist on Cloudinary.");
            } else if (!"ok".equals(resultStatus)) {
                // Fail if status isn't "ok"; indicates partial failure
                throw new IllegalStateException(
                        "Failed to delete image '" + publicId + "'. Cloudinary response: " + resultStatus);
            }

            // Log success for audit trail
            log.info("Image '{}' deleted successfully from Cloudinary.", publicId);
            return true; // Success indicator
        } catch (IOException e) {
            // Wrap I/O errors with context for caller
            throw new IllegalStateException("I/O error while deleting image '" + fileName + "': " + e.getMessage(), e);
        } catch (Exception e) {
            // Wrap unexpected errors with context
            throw new IllegalStateException("Unexpected error deleting image '" + fileName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Updates an image by deleting the old one and uploading a new one.
     *
     * @param oldFileName the name of the existing image to delete
     * @param newFile     the new {@link MultipartFile} image to upload
     * @return the secure URL of the newly uploaded image
     * @throws Exception if deletion or upload fails
     * @implNote Attempts to delete the old image first, then uploads the new one even if deletion fails.
     */
    @Override
    public String updateImage(String oldFileName, MultipartFile newFile) {
        try {
            // Try deleting old image; proceeds even if it fails (e.g., not found)
            if (!deleteImage(oldFileName)) {
                log.warn("Failed to delete old image '{}'. Proceeding with upload of new image.", oldFileName);
            }
            // Upload new image; reuses uploadImage logic
            return uploadImage(newFile);
        } catch (Exception e) {
            // Log error and throw custom message; assumes NO_FILE is appropriate
            log.error("Error update image: {}", e.getMessage());
            throw new IllegalArgumentException(NO_FILE);
        }
    }

    /**
     * Retrieves the preview URL of an image stored on Cloudinary.
     *
     * @param fileName the name of the image file (without folder prefix)
     * @return the secure URL for previewing the image
     * @throws IllegalArgumentException if the image doesn't exist or response is invalid
     * @implNote Queries Cloudinary API for resource details and extracts secure URL.
     */
    @Override
    public String getPreviewUrl(String fileName) {
        // Construct public ID; matches upload structure
        String publicId = folder + "/" + fileName;

        try {
            // Fetch resource metadata; empty options map uses defaults
            Object response = cloudinary.api().resource(publicId, ObjectUtils.emptyMap());

            // Validate response type; expects Map with secure_url
            if (response instanceof Map<?, ?> result) {
                // Extract secure URL safely; handles null or wrong type
                return Optional.ofNullable(result.get("secure_url"))
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Preview URL not found or invalid format for file: " + fileName));
            } else {
                // Fail if response isn't a Map; indicates API issue
                throw new IllegalArgumentException("Unexpected response format from Cloudinary API.");
            }
        } catch (Exception e) {
            // Wrap error with context for caller
            throw new IllegalArgumentException("Error retrieving preview URL: " + e.getMessage(), e);
        }
    }

    /**
     * Validates an image file before upload.
     *
     * @param file the {@link MultipartFile} to validate
     * @throws IllegalArgumentException if file exceeds size limit or has invalid extension
     * @implNote Checks file size against maxFileSize and extension against allowedExtensions.
     * Assumes config values are properly set.
     */
    @Override
    public void validateFile(MultipartFile file) {
        // Check file size; enforces max limit from config (assumed 5MB)
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File exceeds the allowed size limit (5MB).");
        }

        // Extract filename; assumes non-null MultipartFile
        String fileName = file.getOriginalFilename();
        // Validate filename structure; requires extension
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("Invalid file.");
        }

        // Extract extension; case-insensitive comparison
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
        // Split allowed extensions from config (e.g., "jpg,png")
        String[] allowedExtensionsArray = allowedExtensions.split(",");
        // Check if extension is allowed; converts to lowercase for consistency
        if (!Arrays.asList(allowedExtensionsArray).contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file format. Only JPG, JPEG, PNG, and GIF are allowed.");
        }
    }
}