package com.sharesecure.sharesecure.rest.folders;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.sharesecure.sharesecure.entities.payload.CreateFolderRequest;
import com.sharesecure.sharesecure.entities.payload.GiveAccessRequest;

public interface FolderControllerInterface {

    @GetMapping("folder")
    ResponseEntity<?> getRootFolder();
    
    @GetMapping("folder/{id}")
    ResponseEntity<?> getFolder(@PathVariable Long id);

    @PostMapping("folder/{id}")
    ResponseEntity<?> createFolder(@PathVariable("id") Long parentFolderId, @RequestBody CreateFolderRequest requestBody);

    @DeleteMapping("folder/{id}")
    ResponseEntity<?> deleteDirectory(@PathVariable("id") Long folderId);

    // @GetMapping("folder/{id}/download")
    // ResponseEntity<?> downloadFolder(@PathVariable("id") Long folderId);

    // @PostMapping("folder/{id}/access")
    // ResponseEntity<?> shareAccessToFolder(@PathVariable("id") Long folderId, @RequestBody GiveAccessRequest body);

    // @DeleteMapping("folder/{id}/access")
    // ResponseEntity<?> revokeAccessToFolder(@PathVariable("id") Long folderId, @RequestParam("userEmail") String email);
}
