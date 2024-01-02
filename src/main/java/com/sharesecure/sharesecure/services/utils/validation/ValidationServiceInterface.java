package com.sharesecure.sharesecure.services.utils.validation;

import org.springframework.web.multipart.MultipartFile;

public interface ValidationServiceInterface {
    public String sanitizeFileName(String originalFileName);
    public String sanitizePath(String path);
    public String sanitizeEmail(String email);

    public boolean isFileAllowed(MultipartFile file);
    public boolean isFileTypeAllowed(String fileType, String[] allowedFileTypes);
    
}
