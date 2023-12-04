package com.sharesecure.sharesecure.services.files;

import java.io.IOException;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

public interface FileServiceInterface {
    Boolean handleUploadedFile(MultipartFile file, UserDetails user, String sanitizedPath) throws IOException;
}
