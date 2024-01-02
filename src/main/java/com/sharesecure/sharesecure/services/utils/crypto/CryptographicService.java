package com.sharesecure.sharesecure.services.utils.crypto;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import com.sharesecure.sharesecure.entities.CommonMultipartFile;

@Service
public class CryptographicService implements CryptographicServiceInterface {
    @Override
    public File encryptFile(File inputFile, String encryptionKey) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(encryptionKey), "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            byte[] inputBytes = Files.readAllBytes(inputFile.toPath());
            byte[] encryptedBytes = cipher.doFinal(inputBytes);

            File encryptedFile = new File("encrypted_" + inputFile.getName());
            Files.write(encryptedFile.toPath(), encryptedBytes, StandardOpenOption.DELETE_ON_CLOSE);

            return encryptedFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public MultipartFile encryptMultipartFile(MultipartFile inputFile, String encryptionKey) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(encryptionKey), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            byte[] inputBytes = inputFile.getBytes();
            byte[] encryptedBytes = cipher.doFinal(inputBytes);

            MultipartFile encryptedMultipartFile = new CommonMultipartFile(inputFile.getName(), inputFile.getOriginalFilename(), inputFile.getContentType(), encryptedBytes);

            return encryptedMultipartFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public File decryptFile(File encryptedFile, String decryptionKey) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(decryptionKey), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            byte[] encryptedBytes = Files.readAllBytes(encryptedFile.toPath());
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            File tempFile = File.createTempFile(encryptedFile.getName(), "_temp");
            Files.write(Paths.get(tempFile.getPath()), decryptedBytes);

            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String generateRandomKey() {
        try {
            // Generate a key using SecureRandom
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = new SecureRandom();
            keyGenerator.init(secureRandom);

            Key key = keyGenerator.generateKey();

            // Encode the key to Base64 for easy storage and retrieval
            byte[] encodedKey = key.getEncoded();
            return Base64.getEncoder().encodeToString(encodedKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

}
