package com.sharesecure.sharesecure.services;

import java.util.Collection;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import com.sharesecure.sharesecure.dtos.FileDTO;
import com.sharesecure.sharesecure.dtos.FolderDTO;
import com.sharesecure.sharesecure.dtos.FolderReference;
import com.sharesecure.sharesecure.dtos.UserDTO;
import com.sharesecure.sharesecure.dtos.UserPrivilegeDTO;
import com.sharesecure.sharesecure.entities.FileMetaData;
import com.sharesecure.sharesecure.entities.User;
import com.sharesecure.sharesecure.entities.UserPrivilege;
import com.sharesecure.sharesecure.entities.folder.Folder;
import com.sharesecure.sharesecure.entities.folder.RootFolder;
import com.sharesecure.sharesecure.entities.folder.SubFolder;

@Service
public class ModelMapper {

    public FolderDTO convertFolderToDTO(Folder folder, long userId) {
        if (folder instanceof SubFolder) {
            SubFolder subFolder = (SubFolder) Hibernate.unproxy(folder);
            Collection<UserPrivilege> privileges = subFolder.getUserPrivileges();
            UserPrivilege currentUserPrivilege = null;
            for (UserPrivilege userPrivilege : privileges) {
                if (userPrivilege.getUser().getId() == userId) {
                    currentUserPrivilege = userPrivilege;
                    break;
                }
            }

            FolderDTO result = new FolderDTO(
                subFolder.getId(),
                subFolder.getFolderName(),
                subFolder.getSubFiles().stream().map((file) -> convertFileToDTO(file, userId))
                            .collect(Collectors.toList()),
                            subFolder.getSubFolders().stream()
                            .map((fold) -> convertFolderToFolderReference(fold, userId))
                            .collect(Collectors.toList()),
                    convertUserToDTO(subFolder.getFolderOwner()),
                    subFolder.getParentFolder() != null ? convertFolderToFolderReference(subFolder.getParentFolder(), userId)
                            : null,
                    currentUserPrivilege != null ? currentUserPrivilege.getUserPrivileges() : null,
                    subFolder.getSynched());

            return result;
        }
        else{
            RootFolder rootFolder = (RootFolder) Hibernate.unproxy(folder);
    
            FolderDTO result = new FolderDTO(
                    rootFolder.getId(),
                    null,
                    rootFolder.getSubFiles().stream().map((file) -> convertFileToDTO(file, userId)).collect(Collectors.toList()),
                    rootFolder.getSubFolders().stream()
                            .map((fold) -> convertFolderToFolderReference(fold, userId))
                            .collect(Collectors.toList()),
                    convertUserToDTO(rootFolder.getFolderOwner()),
                    null,
                    null,
                    rootFolder.getSynched());
    
            return result;
        }
     
    }

    public FolderReference convertFolderToFolderReference(Folder folder, long userId) {
        if(folder instanceof SubFolder){
            SubFolder subFolder = (SubFolder) Hibernate.unproxy(folder);
            Collection<UserPrivilege> privileges = subFolder.getUserPrivileges();
            UserPrivilege currentUserPrivilege = null;
            for (UserPrivilege userPrivilege : privileges) {
                if (userPrivilege.getUser().getId() == userId) {
                    currentUserPrivilege = userPrivilege;
                    break;
                }
            }
            return new FolderReference(
                subFolder.getId(),
                subFolder.getFolderName(),
                currentUserPrivilege != null ? currentUserPrivilege.getUserPrivileges() : null,
                convertUserToDTO(subFolder.getFolderOwner()),
                subFolder.getSynched());
        }
        else{
            RootFolder rootFolder = (RootFolder) Hibernate.unproxy(folder);
            return new FolderReference(
                rootFolder.getId(),
                null,
                null,
                convertUserToDTO(rootFolder.getFolderOwner()),
                rootFolder.getSynched());
        }
    }

    public FileDTO convertFileToDTO(FileMetaData file, long userId) {
        Collection<UserPrivilege> privileges = file.getUserPrivileges();
        UserPrivilege currentUserPrivilege = null;
        for (UserPrivilege userPrivilege : privileges) {
            if (userPrivilege.getUser().getId() == userId) {
                currentUserPrivilege = userPrivilege;
                break;
            }
        }
        return new FileDTO(
                file.getId(),
                file.getFileName(),
                file.getFileType(),
                currentUserPrivilege != null ? currentUserPrivilege.getUserPrivileges() : null,
                file.getSynched());
    }

    public UserDTO convertUserToDTO(User user) {
        return new UserDTO(user.getEmail());
    }

    public UserPrivilegeDTO convertUserPrivilegeToDTO(UserPrivilege privilege){
        return new UserPrivilegeDTO(
            privilege.getFile() != null ? convertFileToDTO(privilege.getFile(), privilege.getUser().getId()) : null, 
            privilege.getFolder() != null ? convertFolderToFolderReference(privilege.getFolder(), privilege.getUser().getId()) : null, 
            convertUserToDTO(privilege.getUser()), 
            privilege.getUserPrivileges());
    }
}
