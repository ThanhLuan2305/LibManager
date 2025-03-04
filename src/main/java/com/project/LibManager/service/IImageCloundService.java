package com.project.LibManager.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface IImageCloundService {

    String uploadImage(MultipartFile imgUrl) throws IOException;

    boolean deleteImage(String fileName);

    String updateImage(String oldFileName, MultipartFile newFile);

    String getPreviewUrl(String fileName);

    void validateFile(MultipartFile file);

}
