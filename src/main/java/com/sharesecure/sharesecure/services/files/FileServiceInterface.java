package com.sharesecure.sharesecure.services.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import javax.naming.directory.InvalidAttributesException;

import org.springframework.web.multipart.MultipartFile;

import com.sharesecure.sharesecure.entities.FileMetaData;
import com.sharesecure.sharesecure.entities.User;
import com.sharesecure.sharesecure.entities.UserPrivilege;
import com.sharesecure.sharesecure.entities.enums.PrivilegeType;
import com.sharesecure.sharesecure.entities.folder.Folder;

public interface FileServiceInterface {
    public File getFile(Long fileId, User user) throws InvalidAttributesException, FileNotFoundException;
    public Collection<UserPrivilege> getFileAccess(Long fileId, User user) throws InvalidAttributesException;

    public Folder uploadFile(Long parentFolderId, MultipartFile file, User currentUser) throws IOException, InvalidAttributesException;
    public boolean deleteFile(Long fileId, User user) throws InvalidAttributesException;

    public boolean giveFileAccess(Long fileId, User user, PrivilegeType[] privilegeType, User fileOwner) throws InvalidAttributesException;
    public boolean revokeFileAccess(Long fileId, User user, User fileOwner) throws InvalidAttributesException;
    
    public Collection<UserPrivilege> getAccessibleFiles(User user);
    public boolean fileExistsLocally(FileMetaData file);
}
