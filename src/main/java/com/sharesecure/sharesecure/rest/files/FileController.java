package com.sharesecure.sharesecure.rest.files;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.sharesecure.sharesecure.entities.User;
import com.sharesecure.sharesecure.entities.folder.Folder;
import com.sharesecure.sharesecure.entities.payload.GetSharedResponse;
import com.sharesecure.sharesecure.entities.payload.GiveAccessRequest;
import com.sharesecure.sharesecure.entities.payload.SimpleResponse;
import com.sharesecure.sharesecure.repositories.UserRepo;
import com.sharesecure.sharesecure.services.ModelMapper;
import com.sharesecure.sharesecure.services.files.FileServiceInterface;
import com.sharesecure.sharesecure.services.folders.FolderServiceInterface;
import com.sharesecure.sharesecure.services.utils.fileio.FileIOServiceInterface;

@Controller
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class FileController implements FileControllerInterface {

    @Autowired
    UserRepo userRepo;

    @Autowired
    FileServiceInterface fileService;

    @Autowired
    FolderServiceInterface folderService;

    @Autowired
    FileIOServiceInterface fileIOService;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public ResponseEntity<?> uploadFile(Long parentFolderId, MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            User user = userRepo.findByEmail(principal.getUsername()).get();

            Folder folder = fileService.uploadFile(parentFolderId, file, user);

            return ResponseEntity.ok().body(modelMapper.convertFolderToDTO(folder, user.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new SimpleResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> deleteFile(Long fileId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            User user = userRepo.findByEmail(principal.getUsername()).get();

            if (fileService.deleteFile(fileId, user)) {
                return ResponseEntity.ok().body(new SimpleResponse("Successful"));
            }
            return ResponseEntity.badRequest().body(new SimpleResponse("Unsuccessful"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new SimpleResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getAccessList(Long fileId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            User user = userRepo.findByEmail(principal.getUsername()).get();

            return ResponseEntity.ok().body(fileService.getFileAccess(fileId, user).stream().map(
                    privilege -> modelMapper.convertUserPrivilegeToDTO(privilege)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new SimpleResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> shareAccessToFile(Long fileId, GiveAccessRequest body) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            User user = userRepo.findByEmail(principal.getUsername()).get();

            if (userRepo.existsByEmail(body.getUserEmail())) {
                if (fileService.giveFileAccess(fileId, userRepo.findByEmail(body.getUserEmail()).get(),
                        body.getPrivileges(), user)) {
                    return ResponseEntity.ok().body(new SimpleResponse("Successful"));
                } else {
                    return ResponseEntity.badRequest().body(new SimpleResponse("Unsuccessful"));
                }
            } else {
                return ResponseEntity.badRequest().body(new SimpleResponse("User with this email doesn't exists."));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new SimpleResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> revokeAccessToFile(Long fileId, String email) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            User user = userRepo.findByEmail(principal.getUsername()).get();

            if (userRepo.existsByEmail(email)) {
                if (fileService.revokeFileAccess(fileId, userRepo.findByEmail(email).get(), user)) {
                    return ResponseEntity.ok().body(new SimpleResponse("Successful"));
                } else {
                    return ResponseEntity.badRequest().body(new SimpleResponse("Unsuccessful"));
                }
            } else {
                return ResponseEntity.badRequest().body(new SimpleResponse("User with this email doesn't exists."));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new SimpleResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> downloadFile(Long fileId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            User user = userRepo.findByEmail(principal.getUsername()).get();

            File fileToDownload = fileService.getFile(fileId, user);
            try {
                InputStreamResource resource = new InputStreamResource(new FileInputStream(fileToDownload));
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", fileToDownload.getName());
                fileToDownload.delete();
                return ResponseEntity.ok().headers(headers).body(resource);

            } catch (Exception e) {
                return ResponseEntity.badRequest().body(new SimpleResponse(e.getMessage()));
            } finally {
                if (fileToDownload != null && fileToDownload.exists()) {
                    fileToDownload.delete();
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new SimpleResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getSharedContent() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            User user = userRepo.findByEmail(principal.getUsername()).get();

            GetSharedResponse response = new GetSharedResponse();

            response.setUserPrivileges(fileService.getAccessibleFiles(user)
                    .stream()
                    .map(privilege -> modelMapper.convertUserPrivilegeToDTO(privilege))
                    .toList());

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new SimpleResponse(e.getMessage()));
        }
    }
}
