package com.sharesecure.sharesecure.services.utils.validation;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public class ValidationService implements ValidationServiceInterface {
    private final String UPLOAD_DIR = getApplicationLocation() + "\\storage\\users";
    
    @Override
    public String sanitizeFileName(String originalFileName) {
        if (originalFileName.isBlank() || originalFileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        int lastDotIndex = originalFileName.lastIndexOf(".");
        String fileNameWithoutExtension, fileExtension;

        if (lastDotIndex != -1) {
            fileNameWithoutExtension = originalFileName.substring(0, lastDotIndex);
            fileExtension = originalFileName.substring(lastDotIndex);
        } else {
            fileNameWithoutExtension = originalFileName;
            fileExtension = "";
        }

        String sanitizedFileName = fileNameWithoutExtension.replaceAll("[^a-zA-Z0-9_]", "_");

        return sanitizedFileName + fileExtension;
    }

    @Override
    public String sanitizePath(String path) {
        String trimmedPath = path.trim();

        // Resolve the path against a base path to avoid relative path tricks
        Path basePath = Paths.get(UPLOAD_DIR);
        Path resolvedPath = basePath.resolve(trimmedPath);

        // Ensure the resolved path is within the allowed base path
        if (!resolvedPath.startsWith(basePath)) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }

        Path normalizedPath = resolvedPath.normalize();

        String sanitizedPath = normalizedPath.toString();

        if (!sanitizedPath.matches("^[a-zA-Z]:\\\\(?:[^\\\\/:*?\"<>|\\r\\n]+\\\\)*[^\\\\/:*?\"<>|\\r\\n]*$")) {
            throw new IllegalArgumentException("Invalid characters in the path: " + path);
        }

        return sanitizedPath;
    }

    @Override
    public String sanitizeEmail(String email) {
        return email.replaceAll("[^a-zA-Z0-9._-]", "").toLowerCase();
    }

    @Override
    public boolean isFileAllowed(MultipartFile file) {
        String[] allowedFileTypes = { "image/jpeg", "image/png", "application/pdf" };

        long maxFileSize = 5 * 1024 * 1024; //5 MB

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String fileType = file.getContentType();
        if (!isFileTypeAllowed(fileType, allowedFileTypes)) {
            throw new IllegalArgumentException(
                    "Invalid file type. Allowed types are: " + String.join(", ", allowedFileTypes));
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed size.");
        }

        return true;
    }

    @Override
    public boolean isFileTypeAllowed(String fileType, String[] allowedFileTypes) {
        for (String allowedType : allowedFileTypes) {
            if (fileType.equals(allowedType)) {
                return true;
            }
        }
        return false;
    }

    public String getApplicationLocation() {
        ApplicationHome home = new ApplicationHome(getClass());
        
        return home.getSource().getAbsolutePath().substring(0, home.getSource().getAbsolutePath().length() - 15);
    }
}
