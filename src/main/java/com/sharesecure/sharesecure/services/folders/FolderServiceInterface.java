package com.sharesecure.sharesecure.services.folders;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.rmi.ServerException;
import java.util.Collection;

import javax.naming.directory.InvalidAttributesException;

import com.sharesecure.sharesecure.entities.User;
import com.sharesecure.sharesecure.entities.UserPrivilege;
import com.sharesecure.sharesecure.entities.enums.PrivilegeType;
import com.sharesecure.sharesecure.entities.folder.Folder;
import com.sharesecure.sharesecure.entities.folder.SubFolder;

public interface FolderServiceInterface {
    public Folder getFolderById(Long folderID, User user) throws ServerException;

    public Folder createFolder(Long parentFolderId, String folderName, User currentUser) throws IOException, InvalidAttributesException;
    public boolean deleteFolder(Long folderId, User user) throws DirectoryNotEmptyException, IOException, InvalidAttributesException;

    public UserPrivilege giveFolderAccess(Long folderId, User user, PrivilegeType[] privilegeType, User folderOwner) throws InvalidAttributesException;
    public boolean revokeFolderAccess(Long folderId, User user, User folderOwner) throws InvalidAttributesException;

    public Collection<SubFolder> getAccessibleFolders(User user);

    public boolean folderExistsLocally(Folder folder);
    public Folder markFolderUnsynchedElements(Folder folder);
}
