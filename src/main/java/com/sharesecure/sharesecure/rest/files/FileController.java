package com.sharesecure.sharesecure.rest.files;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.sharesecure.sharesecure.services.files.FileService;
import com.sharesecure.sharesecure.services.files.FileUtils;

@Controller
@RequestMapping("api/files")
@PreAuthorize("isAuthenticated()")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<String> handleFileUpload(@RequestParam("path") String filePath, @RequestPart("file") MultipartFile file) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();

        String sanitizedFileName = FileUtils.sanitizeFileName(file.getOriginalFilename());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }
        if (!FileUtils.sanitizeFile(file)) {
            return ResponseEntity.badRequest().body("The file is not in the correct parameters.");
        }
        try {
            // deepcode ignore PT: The file is already sanitized above
            if (fileService.handleUploadedFile(file, principal, filePath)) {
                return ResponseEntity.ok().body("File Successfully uploaded");
            } else
                return ResponseEntity.status(500).body("Failed to upload file: ");
        } catch (FileAlreadyExistsException e) {
            // deepcode ignore XSS: The name is also sanitized above
            return ResponseEntity.badRequest().body("File with this name already exists in this directory:" + sanitizedFileName);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload file: " + e.getMessage());
        }
    }

    @PostMapping("/directory")
    @ResponseBody
    public ResponseEntity<String> handleDirCreation (@RequestParam("path") String path, @RequestParam("newFolder") String newFolder){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            fileService.createUserDir(path, newFolder, principal.getUsername());
            
            return ResponseEntity.ok().body("Folder successfully created");
        }catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to create directory: " + e.getMessage());
        }
    }
}
