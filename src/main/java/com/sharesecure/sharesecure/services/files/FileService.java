package com.sharesecure.sharesecure.services.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.naming.directory.InvalidAttributesException;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sharesecure.sharesecure.entities.FileMetaData;
import com.sharesecure.sharesecure.entities.User;
import com.sharesecure.sharesecure.entities.UserPrivilege;
import com.sharesecure.sharesecure.entities.enums.PrivilegeType;
import com.sharesecure.sharesecure.entities.folder.Folder;
import com.sharesecure.sharesecure.entities.folder.RootFolder;
import com.sharesecure.sharesecure.entities.folder.SubFolder;
import com.sharesecure.sharesecure.repositories.FileMetaDataRepo;
import com.sharesecure.sharesecure.repositories.FolderRepo;
import com.sharesecure.sharesecure.services.utils.crypto.CryptographicServiceInterface;
import com.sharesecure.sharesecure.services.utils.fileio.FileIOServiceInterface;
import com.sharesecure.sharesecure.services.utils.validation.ValidationServiceInterface;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileService implements FileServiceInterface {
    @Autowired
    private FileMetaDataRepo fileMetaDataRepo;

    @Autowired
    private FolderRepo folderRepo;

    @Autowired
    private ValidationServiceInterface validationService;

    @Autowired
    private FileIOServiceInterface fileIOService;

    @Autowired
    private CryptographicServiceInterface cryptographicService;

    @Override
    public Folder uploadFile(Long parentFolderId, MultipartFile file, User currentUser)
            throws IOException, InvalidAttributesException {
        if (folderRepo.existsById(parentFolderId)) {
            Folder parentFolder = (Folder) Hibernate.unproxy(folderRepo.getReferenceById(parentFolderId));
            Path folderPath = fileIOService.getFolderPath(parentFolder);
            
            if (Files.exists(folderPath) && Files.isDirectory(folderPath)) {

                if (parentFolder instanceof RootFolder) {
                    RootFolder folder = (RootFolder) parentFolder;
                    if (folder.getFolderOwner().getId() != currentUser.getId()) {
                        log.error("User is not the owner of the folder");
                        throw new InvalidAttributesException("You can't upload files to folders you don't own.");
                    }
                } else {
                    SubFolder folder = (SubFolder) parentFolder;
                    if (folder.getFolderOwner().getId() != currentUser.getId()) {
                        log.error("User is not the owner of the folder");
                        throw new InvalidAttributesException("You can't upload files to folders you don't own.");
                    }
                }

                    String normalizedFileName = validationService.sanitizeFileName(file.getOriginalFilename());

                    for (FileMetaData fileData : parentFolder.getSubFiles()) {
                        if (fileData.getNormalizedFileName().equals(normalizedFileName)) {
                            log.error("Duplicate normalized name.");
                            throw new InvalidAttributesException("This folder already contains a file with that name.");
                        }
                    }

                    String encryptionKey = cryptographicService.generateRandomKey();

                    MultipartFile encryptedFile = cryptographicService.encryptMultipartFile(file, encryptionKey);
                    fileIOService.uploadFile(encryptedFile
                    , fileIOService.getFolderPath(parentFolder), normalizedFileName);

                    FileMetaData fileData = new FileMetaData();
                    fileData.setFileName(file.getOriginalFilename());
                    fileData.setFileType(file.getContentType());
                    fileData.setNormalizedFileName(normalizedFileName);
                    fileData.setFolder(parentFolder);

                    UserPrivilege userPrivilege = new UserPrivilege();
                    userPrivilege.setFile(fileData);
                    userPrivilege.setOwner(true);
                    userPrivilege.setUser(currentUser);
                    userPrivilege.setUserPrivileges(Arrays.asList(PrivilegeType.values()));
                    userPrivilege.setEncryptionKey(encryptionKey);
                    fileData.addUserPrivilege(userPrivilege);

                    FileMetaData fileMetaData = fileMetaDataRepo.save(fileData);

                    parentFolder.addFile(fileMetaData);
                    return parentFolder;
            } else {
                throw new InvalidAttributesException("The parent folder is present in the DB but no longer exists.");
            }
        } else {
            throw new InvalidAttributesException("There is no parent folder with that ID.");
        }
    }

    @Override
    public Collection<UserPrivilege> getFileAccess(Long fileId, User user) throws InvalidAttributesException {
        if (fileMetaDataRepo.existsById(fileId)) {
            FileMetaData file = (FileMetaData) Hibernate.unproxy(fileMetaDataRepo.getReferenceById(fileId));

            if (Files.exists(fileIOService.getFilePath(file))) {
                log.info("File is valid and exists.");
                if (file.userHasSharingAccess(user)) {
                    return file.getUserPrivileges();
                } else {
                    throw new InvalidAttributesException("You don't have access to this information.");
                }
            } else {
                log.info("File Exists only in DB");
                throw new InvalidAttributesException("Can't get access if the file only exists in DB, please delete the file.");
            }
        } else {
            throw new InvalidAttributesException("Invalid File ID.");
        }
    }

    @Override
    public boolean deleteFile(Long fileId, User user) throws InvalidAttributesException {
        if (fileMetaDataRepo.existsById(fileId)) {
            FileMetaData file = (FileMetaData) Hibernate.unproxy(fileMetaDataRepo.getReferenceById(fileId));

            if (Files.exists(fileIOService.getFilePath(file))) {
                log.info("Folder is valid and exists.");

                if (file.userHasDeleteAccess(user)) {
                    if (fileIOService.deleteFile(fileIOService.getFilePath(file))) {
                        fileMetaDataRepo.delete(file);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    throw new InvalidAttributesException("You don't have delete access.");
                }
            } else {
                log.info("File Exists only in DB");

                if (file.getFileOwner().equals(user)) {
                    fileMetaDataRepo.delete(file);
                    return true;
                } else {
                    throw new InvalidAttributesException("You can't delete folders you don't own.");
                }
            }
        } else {
            throw new InvalidAttributesException("Invalid File ID.");
        }
    }

    @Override
    public boolean giveFileAccess(Long fileId, User user, PrivilegeType[] privilegeType, User requestMaker)
            throws InvalidAttributesException {
        if (fileMetaDataRepo.existsById(fileId)) {
            FileMetaData file = (FileMetaData) Hibernate.unproxy(fileMetaDataRepo.getReferenceById(fileId));

            if (Files.exists(fileIOService.getFilePath(file))) {
                log.info("File is valid and exists.");
                if (file.userHasSharingAccess(requestMaker)) {

                    if (!file.userHasAnyAccess(user)) {
                        UserPrivilege userPrivilege = new UserPrivilege();
                        userPrivilege.setFile(file);
                        userPrivilege.setOwner(false);
                        userPrivilege.setUser(user);
                        userPrivilege.setUserPrivileges(Arrays.asList(privilegeType));
                        file.addUserPrivilege(userPrivilege);

                        fileMetaDataRepo.save(file);
                        return true;
                    } else {
                        throw new InvalidAttributesException(
                                "User already has access. To change his access, revoke it and give him new one.");
                    }
                } else {
                    throw new InvalidAttributesException("You can't share access for files you don't have sharing access.");
                }
            } else {
                log.info("File Exists only in DB");
                throw new InvalidAttributesException("Can't share access for a file that exists only in DB.");
            }
        } else {
            throw new InvalidAttributesException("Invalid File ID.");
        }
    }

    @Override
    public boolean revokeFileAccess(Long fileId, User user, User requestMaker) throws InvalidAttributesException {
        if (fileMetaDataRepo.existsById(fileId)) {
            FileMetaData file = (FileMetaData) Hibernate.unproxy(fileMetaDataRepo.getReferenceById(fileId));

            if (Files.exists(fileIOService.getFilePath(file))) {
                log.info("File is valid and exists.");
                if (file.userHasSharingAccess(requestMaker)) {

                    if (file.userHasAnyAccess(user)) {
                        if(!file.getFileOwner().equals(user)){
                            file.removeUserPrivilege(file.getUserPrivilegeForUser(user));
                            fileMetaDataRepo.save(file);
                            return true;
                        }
                        else{
                            throw new InvalidAttributesException(
                                "You can't revoke access to file owner.");
                        }
                    } else {
                        throw new InvalidAttributesException(
                                "You can't revoke access to users that don't have access.");
                    }
                } else {
                    throw new InvalidAttributesException("You can't revoke access for files you don't own.");
                }
            } else {
                log.info("File Exists only in DB");
                throw new InvalidAttributesException("Can't revoke access for a file that exists only in DB.");
            }
        } else {
            throw new InvalidAttributesException("Invalid File ID.");
        }
    }

    @Override
    public File getFile(Long fileId, User user) throws InvalidAttributesException, FileNotFoundException {
        if (fileMetaDataRepo.existsById(fileId)) {
            FileMetaData file = (FileMetaData) Hibernate.unproxy(fileMetaDataRepo.getReferenceById(fileId));

            if (Files.exists(fileIOService.getFilePath(file))) {
                if (file.userHasDownloadAccess(user)) {
                    File fileToDownload = fileIOService.getFilePath(file).toFile();
                    String key = file.getUserPrivilegeForUser(user).getEncryptionKey();

                    File decryptedFile = cryptographicService.decryptFile(fileToDownload, key);
                    return decryptedFile;
                } else {
                    throw new InvalidAttributesException("You can't download files you don't have access to.");
                }
            } else {
                log.info("File Exists only in DB");
                throw new InvalidAttributesException("Can't download a file that exists only in DB.");
            }
        } else {
            throw new InvalidAttributesException("Invalid File ID.");
        }
    }

    @Override
    public Collection<UserPrivilege> getAccessibleFiles(User user) {
        Collection<UserPrivilege> result = new ArrayList<UserPrivilege>();
        user.getAccessibleContent().forEach(userPrivilege -> {
            if (userPrivilege.getFile() != null && !userPrivilege.isOwner()) {
                if (!fileExistsLocally(userPrivilege.getFile()))
                    userPrivilege.getFile().setSynched(false);
                result.add(userPrivilege);
            }
        });

        return result;
    }

    @Override
    public boolean fileExistsLocally(FileMetaData file) {
        Path filePath = fileIOService.getFilePath(file);

        if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
            return true;
        } else {
            log.error("File no longer exists");
            return false;
        }
    }
}