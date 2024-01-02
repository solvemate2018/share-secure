package com.sharesecure.sharesecure.services.utils.crypto;

import java.io.File;

import org.springframework.web.multipart.MultipartFile;

public interface CryptographicServiceInterface {
    public File encryptFile(File inputFile, String encryptionKey);
    public MultipartFile encryptMultipartFile(MultipartFile inputFile, String encryptionKey);
    public File decryptFile(File encryptedFile, String decryptionKey);
    public String generateRandomKey();
}
