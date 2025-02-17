package com.project.LibManager.service;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


public interface IImageCloundService {
    
    public String uploadImage(MultipartFile imgUrl);
    

    public boolean deleteImage(String fileName);
    
    public String updateImage(String oldFileName, MultipartFile newFile) throws IOException;
    

    public String getPreviewUrl(String fileName);

    public void validateFile(MultipartFile file);
    
}
