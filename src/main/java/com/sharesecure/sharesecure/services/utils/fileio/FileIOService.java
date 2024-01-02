package com.sharesecure.sharesecure.services.utils.fileio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.naming.directory.InvalidAttributesException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sharesecure.sharesecure.entities.FileMetaData;
import com.sharesecure.sharesecure.entities.folder.Folder;
import com.sharesecure.sharesecure.entities.folder.RootFolder;
import com.sharesecure.sharesecure.entities.folder.SubFolder;
import com.sharesecure.sharesecure.services.utils.validation.ValidationServiceInterface;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class FileIOService implements FileIOServiceInterface {

    private final String UPLOAD_DIR = getApplicationLocation() + "\\storage\\users";

    @Autowired
    private ValidationServiceInterface validationService;

    @PostConstruct
    public void initialize() {
        createStorageFolder();
    }

    @Override
    public Path createDir(String path) throws IOException {
        Path repository = Paths.get(validationService.sanitizePath(path));

        if (!Files.exists(repository)) {
            Files.createDirectories(repository);
        } else {
            throw new IllegalArgumentException("Folder already exists at this path.");
        }
        return repository;
    }

    @Override
    public Path uploadFile(MultipartFile file, Path uploadDir, String fileName) throws IOException, InvalidAttributesException {
        if (validationService.isFileAllowed(file)) {
            validationService.sanitizePath(uploadDir.toString());
            InputStream inputStream = file.getInputStream();
            Path destFilePath = uploadDir.resolve(fileName);
            Files.copy(inputStream, destFilePath);
            return destFilePath;

        }
        else{
            throw new InvalidAttributesException("File is not allowed on the server.");
        }
    }

    @Override
    public Path getFolderPath(Folder folder) {
        if (folder instanceof RootFolder) {
            return Path.of(UPLOAD_DIR, folder.getFolderNormalizedName());
        } else {
            SubFolder subFolder = (SubFolder) folder;
            Folder parentFolder = subFolder.getParentFolder();
            String path = File.separator + folder.getFolderNormalizedName();

            while (parentFolder != null) {
                path = parentFolder.getFolderNormalizedName().concat(File.separator + path);
                if (parentFolder instanceof SubFolder) {
                    SubFolder fSubFolder = (SubFolder) parentFolder;
                    parentFolder = fSubFolder.getParentFolder();
                } else {
                    break;
                }
            }

            Path folderPath = Paths.get(UPLOAD_DIR + File.separator + path);
            return folderPath;
        }
    }

    @Override
    public Path getFilePath(FileMetaData file) {
        Folder parentFolder = file.getFolder();
        String path = File.separator + file.getNormalizedFileName();

        while (parentFolder != null) {
            path = parentFolder.getFolderNormalizedName().concat(File.separator + path);
            if (parentFolder instanceof SubFolder) {
                SubFolder fSubFolder = (SubFolder) parentFolder;
                parentFolder = fSubFolder.getParentFolder();
            } else {
                break;
            }
        }
        Path folderPath = Paths.get(UPLOAD_DIR + File.separator + path);
        return folderPath;
    }

    @Override
    public boolean deleteDir(Path path) throws DirectoryNotEmptyException, IOException {
        validationService.sanitizePath(path.toString());
        if (Files.walk(path, FileVisitOption.FOLLOW_LINKS)
                .skip(1)
                .findFirst().isPresent()) {
            throw new DirectoryNotEmptyException("The folder you are trying to delete is not empty.");
        }

        Files.delete(path);
        return true;
    }

    @Override
    public boolean deleteFile(Path path) {
        try {
            validationService.sanitizePath(path.toString());
            Files.delete(path);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getApplicationLocation() {
        ApplicationHome home = new ApplicationHome(getClass());
        
        return home.getSource().getAbsolutePath().substring(0, home.getSource().getAbsolutePath().length() - 15);
    }

    public void createStorageFolder() {
        Path path = Path.of(UPLOAD_DIR);
        if(!Files.exists(path)){
            try{
                Files.createDirectories(path);
            }
            catch (Exception e) {
            }
        }
    }

}
