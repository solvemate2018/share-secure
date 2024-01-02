package com.sharesecure.sharesecure.services.folders;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.naming.directory.InvalidAttributesException;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sharesecure.sharesecure.entities.FileMetaData;
import com.sharesecure.sharesecure.entities.User;
import com.sharesecure.sharesecure.entities.UserPrivilege;
import com.sharesecure.sharesecure.entities.enums.PrivilegeType;
import com.sharesecure.sharesecure.entities.folder.Folder;
import com.sharesecure.sharesecure.entities.folder.RootFolder;
import com.sharesecure.sharesecure.entities.folder.SubFolder;
import com.sharesecure.sharesecure.repositories.FolderRepo;
import com.sharesecure.sharesecure.services.files.FileServiceInterface;
import com.sharesecure.sharesecure.services.utils.fileio.FileIOServiceInterface;
import com.sharesecure.sharesecure.services.utils.validation.ValidationServiceInterface;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FolderService implements FolderServiceInterface {

    @Autowired
    private FolderRepo folderRepo;

    @Autowired
    private ValidationServiceInterface validationService;

    @Autowired
    private FileIOServiceInterface fileIOService;

    @Autowired
    private FileServiceInterface fileService;

    @Override
    public Folder getFolderById(Long folderID, User user) throws ServerException {
        if(folderRepo.existsById(folderID)){
            Folder folder = (Folder) Hibernate.unproxy(folderRepo.getReferenceById(folderID));
            if(folderExistsLocally(folder)){
                if(folder instanceof RootFolder){
                    if(user.getRootFolder().equals(folder)){
                        return folder;
                    }
                    else{
                        throw new IllegalArgumentException("User is not the owner of this folder.");
                    }
                }
                else{
                    SubFolder subFolder = (SubFolder) folder;

                    if(subFolder.userHasAccess(user)){
                        return subFolder;
                    }
                    else{
                        throw new IllegalArgumentException("User doesn't have access.");
                    }
                }
            }
            else{
                if(folder instanceof RootFolder){
                    throw new ServerException("It seems your root folder no longer exists on the server. Please contact support.");
                }
                else{
                    SubFolder subFolder = (SubFolder) folder;

                    if(subFolder.userHasAccess(user)){
                        subFolder.setSynched(false);
                        return subFolder;
                    }
                    else{
                        throw new IllegalArgumentException("User doesn't have access.");
                    }
                }
            }
        }
        throw new IllegalArgumentException("There is no folder with that ID.");
    }

    @Override
    public Folder createFolder(Long parentFolderId, String folderName, User currentUser) throws IOException, InvalidAttributesException {
        //Validate parentFolder
        if (folderRepo.existsById(parentFolderId)) {
            Folder parentFolder = (Folder) Hibernate.unproxy(folderRepo.getReferenceById(parentFolderId));
            if (folderExistsLocally(parentFolder)) {
                if(parentFolder instanceof RootFolder){
                    RootFolder folder = (RootFolder) parentFolder;
                    if(folder.getFolderOwner().equals(currentUser)){
                        SubFolder newFolder = new SubFolder();
                        newFolder.setFolderName(folderName);
                        newFolder.setFolderNormalizedName(validationService.sanitizeFileName(folderName));
                        newFolder.setParentFolder(parentFolder);
        
                        UserPrivilege userPrivilege = new UserPrivilege();
                        userPrivilege.setOwner(true);
                        userPrivilege.setUser(currentUser);
                        userPrivilege.setFolder(newFolder);
                        userPrivilege.setUserPrivileges(Arrays.asList(PrivilegeType.values()));
                        newFolder.addUserPrivilege(userPrivilege);
        
                        fileIOService.createDir(fileIOService.getFolderPath(parentFolder) + File.separator + newFolder.getFolderNormalizedName());
        
                        Folder folderResult = folderRepo.save(newFolder);
                        return folderResult;
                    }
                    else{
                        throw new InvalidAttributesException("You can't create subfolder in that folder.");
                    }
                }
                else{
                    SubFolder folder = (SubFolder) parentFolder;
                    if(folder.getFolderOwner().equals(currentUser)){
                        SubFolder newFolder = new SubFolder();
                        newFolder.setFolderName(folderName);
                        newFolder.setFolderNormalizedName(validationService.sanitizeFileName(folderName));
                        newFolder.setParentFolder(parentFolder);
        
                        UserPrivilege userPrivilege = new UserPrivilege();
                        userPrivilege.setOwner(true);
                        userPrivilege.setUser(currentUser);
                        userPrivilege.setFolder(newFolder);
                        userPrivilege.setUserPrivileges(Arrays.asList(PrivilegeType.values()));
                        newFolder.addUserPrivilege(userPrivilege);
        
                        fileIOService.createDir(fileIOService.getFolderPath(parentFolder) + File.separator + newFolder.getFolderNormalizedName());
        
                        Folder folderResult = folderRepo.save(newFolder);
                        return folderResult;
                    }
                    else{
                        throw new InvalidAttributesException("You can't create subfolder in that folder.");
                    }
                }
                
            } else {
                throw new InvalidAttributesException("The parent folder is present in the DB but no longer exists.");
            }
        }
        throw new InvalidAttributesException("There is no parent folder with that ID.");
    }

    @Override
    public boolean deleteFolder(Long folderId, User user) throws DirectoryNotEmptyException, IOException, InvalidAttributesException {
        //Verify FolderID Exists
        if (folderRepo.existsById(folderId)) {
            Folder folder = (Folder) Hibernate.unproxy(folderRepo.getReferenceById(folderId));
            //Verify folderExistsLocally
            if (folderExistsLocally(folder)) {
                //Verify it's not the rootFolder
                if (folder instanceof RootFolder) {
                    throw new InvalidAttributesException("You can't delete your root Folder");
                } else {
                    SubFolder subfolder = (SubFolder) folder;
                    //Verify folder owner
                    if (subfolder.getFolderOwner() == user) {
                        //Verify folder is empty in the db
                        if (subfolder.getSubFolders().size() > 0 || subfolder.getSubFiles().size() > 0) {
                            throw new InvalidAttributesException("You can't delete folder if they still have content.");
                        } else {
                            if (fileIOService.deleteDir(fileIOService.getFolderPath(subfolder))) {
                                folderRepo.delete(subfolder);
                                return true;
                            } else {
                                return false;
                            }
                        }
                    } else {
                        throw new InvalidAttributesException("You can't delete folders you don't own.");
                    }
                }
            } else {
                //The folder exists only in the db
                if (folder instanceof RootFolder) {
                    throw new InvalidAttributesException("You can't delete your root Folder");
                } else {
                    SubFolder subfolder = (SubFolder) folder;
                    if (subfolder.getFolderOwner() == user) {
                        if (subfolder.getSubFolders().size() > 0 || subfolder.getSubFiles().size() > 0) {
                            throw new InvalidAttributesException("You can't delete folder if they still have content.");
                        } else {
                            //Delete the folder from the db
                            folderRepo.delete(subfolder);
                            return true;
                        }
                    } else {
                        throw new InvalidAttributesException("You can't delete folders you don't own.");
                    }
                }
            }
        } else {
            throw new InvalidAttributesException("Invalid Folder ID.");
        }
    }

    @Override
    public UserPrivilege giveFolderAccess(Long folderId, User user, PrivilegeType[] privilegeType, User folderOwner) throws InvalidAttributesException {
        //Verify folder ID
        if (folderRepo.existsById(folderId)) {
            Folder folder = (Folder) Hibernate.unproxy(folderRepo.getReferenceById(folderId));
            //Verify it's not the RootFolder
            if (folder instanceof RootFolder) {
                throw new InvalidAttributesException(
                        "Can't share access for your root folder.");
            }
            SubFolder subFolder = (SubFolder) folder;

            //Verify it exists locally
            if (folderExistsLocally(folder)) {
                //Verify that the folder owner is making changes
                if (subFolder.getFolderOwner().equals(folderOwner)) {
                    //Verify that the user who is receiving access, didn't had any before
                    if (!subFolder.userHasAccess(user)) {
                        UserPrivilege userPrivilege = new UserPrivilege();
                        userPrivilege.setFolder(subFolder);
                        userPrivilege.setOwner(false);
                        userPrivilege.setUser(user);
                        userPrivilege.setUserPrivileges(Arrays.asList(privilegeType));
                        subFolder.addUserPrivilege(userPrivilege);

                        folderRepo.save(subFolder);
                        return userPrivilege;
                    } else {
                        throw new InvalidAttributesException(
                                "User already has access. To change his access, revoke it and give him new one.");
                    }
                } else {
                    throw new InvalidAttributesException("You can't share access for folders you don't own.");
                }
            } else {
                log.info("Folder Exists only in DB");
                throw new InvalidAttributesException("Can't share access for a folder that exists only in DB.");
            }
        } else {
            throw new InvalidAttributesException("Invalid Folder ID.");
        }
    }

    @Override
    public boolean revokeFolderAccess(Long folderId, User user, User folderOwner) throws InvalidAttributesException {
        //Verify folder ID
        if (folderRepo.existsById(folderId)) {
            Folder folder = (Folder) Hibernate.unproxy(folderRepo.getReferenceById(folderId));

            //Verify it's not the RootFolder
            if (folder instanceof RootFolder) {
                throw new InvalidAttributesException(
                        "Can't revoke access for your root folder.");
            }

            SubFolder subFolder = (SubFolder) folder;

            //Verify it exists locally
            if (folderExistsLocally(folder)) {
                //Verify that the folder owner is making changes
                if (subFolder.getFolderOwner().equals(folderOwner)) {
                    //Verify that the user whose access is taken, had any access in the first place
                    if (subFolder.userHasAccess(user)) {
                        //Remove the userPrivilege for the folder
                        subFolder.removeUserPrivilege(subFolder.getUserPrivilegeForUser(user));
                        folderRepo.save(subFolder);
                        return true;
                    } else {
                        throw new InvalidAttributesException(
                                "User doesn't have access to this folder.");
                    }
                } else {
                    throw new InvalidAttributesException("You can't revoke access for folders you don't own.");
                }
            } else {
                throw new InvalidAttributesException("Can't revoke access for a folder that exists only in DB.");
            }
        } else {
            throw new InvalidAttributesException("Invalid Folder ID.");
        }
    }

    @Override
    public Collection<SubFolder> getAccessibleFolders(User user) {
        Collection<SubFolder> result = new ArrayList<SubFolder>();
        user.getAccessibleContent().forEach(userPrivilege -> {
            if (userPrivilege.getFolder() != null && !userPrivilege.isOwner()) {
                if(!folderExistsLocally(userPrivilege.getFolder())){
                    userPrivilege.getFolder().setSynched(false);
                }
                result.add((SubFolder) userPrivilege.getFolder());
            }
        });

        return result;
    }

    @Override
    public boolean folderExistsLocally(Folder folder) {
        Path folderPath = fileIOService.getFolderPath(folder);

        if (Files.exists(folderPath) && Files.isDirectory(folderPath)) {
            return true;
        } else {
            log.error("Folder no longer exists");
            return false;
        }
    }

    public Folder markFolderUnsynchedElements(Folder folder) {
        if(!folderExistsLocally(folder)){
            folder.setSynched(false);
        }

        Collection<SubFolder> folderContent = folder.getSubFolders();

        for (SubFolder nestedFolder : folderContent) {
            if(!folderExistsLocally(nestedFolder))
                nestedFolder.setSynched(false);
        }

        Collection<FileMetaData> fileContent = folder.getSubFiles();

        for (FileMetaData nestedFile : fileContent) {
            if(!fileService.fileExistsLocally(nestedFile))
                nestedFile.setSynched(false);
        }

        return folder;
    }
}
