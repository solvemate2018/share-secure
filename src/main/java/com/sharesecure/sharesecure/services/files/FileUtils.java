package com.sharesecure.sharesecure.services.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class FileUtils {
    private static final String UPLOAD_DIR = "C:\\Users\\georg\\Desktop\\EASV Exams 1st semester\\Software Sec\\share-secure\\storage\\users";

    public static String sanitizeFileName(String originalFileName) {
        if (originalFileName != null && originalFileName.equals("")) {
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

    public static boolean sanitizeFile(MultipartFile file) {
        String[] allowedFileTypes = { "image/jpeg", "image/png", "application/pdf" };

        long maxFileSize = 5 * 1024 * 1024;

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
    };

    public static String sanitizePath(String userInput) {
        String trimmedPath = userInput.trim();

        // Resolve the path against a base path to avoid relative path tricks
        Path basePath = Paths.get(UPLOAD_DIR);
        Path resolvedPath = basePath.resolve(trimmedPath);

        // Ensure the resolved path is within the allowed base path
        if (!resolvedPath.startsWith(basePath)) {
            throw new IllegalArgumentException("Invalid path: " + userInput);
        }

        Path normalizedPath = resolvedPath.normalize();

        String sanitizedPath = normalizedPath.toString();

        return sanitizedPath;
    }

    private static boolean isFileTypeAllowed(String fileType, String[] allowedFileTypes) {
        for (String allowedType : allowedFileTypes) {
            if (fileType.equals(allowedType)) {
                return true;
            }
        }
        return false;
    }

    public static String getFileType(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        } else {
            // No file extension found
            return "";
        }
    }

    public static String sanitizeEmail(String email) {
        return email.replaceAll("[^a-zA-Z0-9._-]", "").toLowerCase();
    }

    public static Path createDir(String path) throws IOException{
        Path repository = Paths.get(path);

        if (!Files.exists(repository)) {
            Files.createDirectories(repository);
        }
        return repository;
    }

    public static Path uploadFile(InputStream inputStream, Path uploadDirPath, String fileName) throws IOException {
        Path destFilePath = uploadDirPath.resolve(fileName);
        Files.copy(inputStream, destFilePath);
        return destFilePath;
    }
}
