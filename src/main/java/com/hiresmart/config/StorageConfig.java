package com.hiresmart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.File;

@Configuration
public class StorageConfig {

    @Value("${hiresmart.storage.upload-dir:uploads/resumes}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public String getUploadDir() {
        return uploadDir;
    }
}
