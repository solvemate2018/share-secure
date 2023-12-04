package com.sharesecure.sharesecure.services.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.sharesecure.sharesecure.entities.FileMetaData;
import com.sharesecure.sharesecure.entities.User;
import com.sharesecure.sharesecure.entities.UserFileMapping;
import com.sharesecure.sharesecure.entities.enums.UserPrivileges;
import com.sharesecure.sharesecure.repositories.FileMetaDataRepo;
import com.sharesecure.sharesecure.repositories.UserRepo;

import ch.qos.logback.core.util.FileUtil;

@Service
public class FileService implements FileServiceInterface {
    private static final String UPLOAD_DIR = "C:\\Users\\georg\\Desktop\\EASV Exams 1st semester\\Software Sec\\share-secure\\storage\\users";

    @Autowired
    private FileMetaDataRepo fileMetaDataRepo;

    @Autowired
    private UserRepo userRepo;

    @Transactional(rollbackFor = IOException.class)
    @Override
    public Boolean handleUploadedFile(MultipartFile file, UserDetails user, String unsanitizedPath) throws IOException {
        try {
            // Set up necessary variables
            String username = user.getUsername();
            Path uploadDirPath = Paths.get(FileUtils.sanitizePath(UPLOAD_DIR + "\\" + FileUtils.sanitizeEmail(username) + "\\" + unsanitizedPath));
            Optional<User> optionalDbUser = userRepo.findByEmail(username);

            // Create the user directory if it doesn't exists
            try {
                FileUtils.createDir(UPLOAD_DIR + "\\" + FileUtils.sanitizeEmail(username));
            } catch (IOException e) {
            }

            // Validate directory path
            if (!uploadDirPath.toRealPath().startsWith(Paths.get(UPLOAD_DIR))) {
                throw new IOException("Creating file " + uploadDirPath.getFileName() +
                        " would expand outside of " + uploadDirPath.toRealPath());
            }

            // Upload the file to the new directory
            String fileName = FileUtils.sanitizeFileName(file.getOriginalFilename());
            FileUtils.uploadFile(file.getInputStream(), uploadDirPath, fileName);

            // Prepare the file data to save in the DB
            FileMetaData fileData = new FileMetaData();
            fileData.setFileName(fileName);
            String fileExtension = FileUtils.getFileType(fileName);
            fileData.setFileType(fileExtension);

            if (optionalDbUser.isPresent()) {
                User dbUser = optionalDbUser.get();
                saveUserFileMapping(fileData, dbUser);

                if (!dbUser.hasAccessToFile(fileName)) {
                    return false;
                }
            }

            return true;
        } catch (FileAlreadyExistsException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Error handling uploaded file", ex);
        }
    }

    public void createUserDir(String path, String newFolder, String userFolder) throws IOException {
        try {
            String sanitizedPath = FileUtils.sanitizePath(path);
            String sanitizedFolderName = FileUtils.sanitizeFileName(newFolder);
            String sanitizedUserFolder = FileUtils.sanitizeEmail(userFolder);

            FileUtils.createDir(sanitizedPath + "\\" + sanitizedUserFolder + "\\" + sanitizedFolderName);
        } catch (IOException e) {
            throw e;
        }
    }

    private void saveUserFileMapping(FileMetaData fileData, User dbUser) {
        UserFileMapping userFile = new UserFileMapping();
        userFile.setFileMetaData(fileData);
        userFile.setOwner(true);
        userFile.setUser(dbUser);
        userFile.setUserPrivileges(UserPrivileges.READ_WRITE);

        fileData.addUserWithAccess(userFile);
        fileMetaDataRepo.save(fileData);
    }
}
