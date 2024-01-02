package com.sharesecure.sharesecure.rest.files;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.sharesecure.sharesecure.entities.payload.GiveAccessRequest;

public interface FileControllerInterface {
    
    @PostMapping("file/{id}")
    ResponseEntity<?> uploadFile(@PathVariable("id") Long parentFolderId, @RequestParam("file") MultipartFile file);

    @DeleteMapping("file/{id}")
    ResponseEntity<?> deleteFile(@PathVariable("id") Long fileId);
    
    @GetMapping("file/{id}/access")
    ResponseEntity<?> getAccessList(@PathVariable("id") Long fileId);

    @PostMapping("file/{id}/access")
    ResponseEntity<?> shareAccessToFile(@PathVariable("id") Long fileId, @RequestBody GiveAccessRequest body);

    @DeleteMapping("file/{id}/access")
    ResponseEntity<?> revokeAccessToFile(@PathVariable("id") Long fileId, @RequestParam("userEmail") String email);

    @GetMapping("file/{id}/download")
    ResponseEntity<?> downloadFile(@PathVariable("id") Long fileId);

    @GetMapping("shared")
    ResponseEntity<?> getSharedContent();
}

