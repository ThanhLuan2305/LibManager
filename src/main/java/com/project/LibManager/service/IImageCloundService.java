package com.project.LibManager.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface IImageCloundService {
    
    public String uploadImage(MultipartFile imgUrl);
    

    public boolean deleteImage(String fileName);
    
    public String updateImage(String oldFileName, MultipartFile newFile) throws IOException;
    

    public String getPreviewUrl(String fileName);

    public void validateFile(MultipartFile file);
    
}
