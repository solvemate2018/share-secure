package com.sharesecure.sharesecure.rest.folders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sharesecure.sharesecure.dtos.FolderDTO;
import com.sharesecure.sharesecure.entities.User;
import com.sharesecure.sharesecure.entities.folder.Folder;
import com.sharesecure.sharesecure.entities.payload.CreateFolderRequest;
import com.sharesecure.sharesecure.entities.payload.GiveAccessRequest;
import com.sharesecure.sharesecure.entities.payload.SimpleResponse;
import com.sharesecure.sharesecure.repositories.UserRepo;
import com.sharesecure.sharesecure.services.ModelMapper;
import com.sharesecure.sharesecure.services.folders.FolderServiceInterface;

@Controller
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
public class FolderController implements FolderControllerInterface {

    @Autowired
    UserRepo userRepo;

    @Autowired
    FolderServiceInterface folderService;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public ResponseEntity<?> getRootFolder() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            User user = userRepo.findByEmail(principal.getUsername()).get();

            Folder folder;
            folder = folderService.getFolderById(user.getRootFolder().getId(), user);

            FolderDTO result = modelMapper.convertFolderToDTO(folderService.markFolderUnsynchedElements(folder), user.getId());

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new SimpleResponse(ex.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> getFolder(Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            User user = userRepo.findByEmail(principal.getUsername()).get();

            Folder folder = folderService.getFolderById(id, user);

            FolderDTO result = modelMapper.convertFolderToDTO(folderService.markFolderUnsynchedElements(folder), user.getId());

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new SimpleResponse(ex.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> createFolder(Long parentFolderId, CreateFolderRequest requestBody) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            User user = userRepo.findByEmail(principal.getUsername()).get();

            Folder folder = folderService.createFolder(parentFolderId, requestBody.getFolderName(), user);

            return ResponseEntity.ok().body(modelMapper.convertFolderToDTO(folder, user.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new SimpleResponse(e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> deleteDirectory(Long folderId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            User user = userRepo.findByEmail(principal.getUsername()).get();

            if (folderService.deleteFolder(folderId, user)) {
                return ResponseEntity.ok().body(new SimpleResponse("Successful"));
            }
            return ResponseEntity.badRequest().body(new SimpleResponse("Unsuccessful"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new SimpleResponse(e.getMessage()));
        }
    }


    // @Override
    // public ResponseEntity<?> downloadFolder(Long folderId) {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'downloadFolder'");
    // }

    // @Override
    // public ResponseEntity<?> shareAccessToFolder(Long folderId, GiveAccessRequest body) {
    //     try {
    //         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //         UserDetails principal = (UserDetails) authentication.getPrincipal();
    //         User user = userRepo.findByEmail(principal.getUsername()).get();

    //         if (userRepo.existsByEmail(body.getUserEmail())) {
    //                 return ResponseEntity
    //                 .ok()
    //                 .body(
    //                     modelMapper.convertUserPrivilegeToDTO(
    //                     folderService
    //                     .giveFolderAccess(
    //                         folderId, 
    //                         userRepo.findByEmail(body.getUserEmail()).get(), 
    //                         body.getPrivileges(), 
    //                         user)));
    //         } else {
    //             return ResponseEntity.badRequest().body("User with this email doesn't exists.");
    //         }

    //     } catch (Exception e) {
    //         return ResponseEntity.badRequest().body(e.getMessage());
    //     }
    // }

    // @Override
    // public ResponseEntity<?> revokeAccessToFolder(Long folderId, String email) {
    //     try {
    //         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //         UserDetails principal = (UserDetails) authentication.getPrincipal();
    //         User user = userRepo.findByEmail(principal.getUsername()).get();

    //         if (userRepo.existsByEmail(email)) {
    //             if (folderService.revokeFolderAccess(folderId, userRepo.findByEmail(email).get(), user)) {
    //                 return ResponseEntity.ok().body("Successful");
    //             } else {
    //                 return ResponseEntity.badRequest().body("UnSuccessful");
    //             }
    //         } else {
    //             return ResponseEntity.badRequest().body("User with this email doesn't exists.");
    //         }

    //     } catch (Exception e) {
    //         return ResponseEntity.badRequest().body(e.getMessage());
    //     }
    // }
}
