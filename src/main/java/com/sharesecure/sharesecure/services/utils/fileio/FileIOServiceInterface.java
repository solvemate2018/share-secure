package com.sharesecure.sharesecure.services.utils.fileio;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Path;

import javax.naming.directory.InvalidAttributesException;

import org.springframework.web.multipart.MultipartFile;

import com.sharesecure.sharesecure.entities.FileMetaData;
import com.sharesecure.sharesecure.entities.folder.Folder;

public interface FileIOServiceInterface {
    public Path createDir(String path) throws IOException;
    public Path uploadFile(MultipartFile file, Path uploadDir, String fileName) throws IOException, InvalidAttributesException;

    public Path getFolderPath(Folder folder);
    public Path getFilePath(FileMetaData file);

    public boolean deleteDir(Path path) throws DirectoryNotEmptyException, IOException;
    public boolean deleteFile(Path path);
}
