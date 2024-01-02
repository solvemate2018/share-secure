package com.sharesecure.sharesecure.servicesTesting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;

import javax.naming.directory.InvalidAttributesException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.mock.web.MockMultipartFile;

import com.sharesecure.sharesecure.entities.FileMetaData;
import com.sharesecure.sharesecure.entities.folder.RootFolder;
import com.sharesecure.sharesecure.entities.folder.SubFolder;
import com.sharesecure.sharesecure.services.utils.fileio.FileIOService;
import com.sharesecure.sharesecure.services.utils.fileio.FileIOServiceInterface;
import com.sharesecure.sharesecure.services.utils.validation.ValidationServiceInterface;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class FileIOServiceTest {

    @Mock
    private ValidationServiceInterface validationService;

    @InjectMocks
    private FileIOService fileIOService;

    private final String UPLOAD_DIR = getApplicationLocation() + "\\storage\\users";

    public String getApplicationLocation() {
        ApplicationHome home = new ApplicationHome(getClass());
        
        return home.getSource().getAbsolutePath().substring(0, home.getSource().getAbsolutePath().length() - 15);
    }

    @BeforeEach
    void setUp() {
        when(validationService.sanitizePath(anyString())).thenCallRealMethod();
    }

    @Test
    public void createDir_Success() throws IOException {
        // Arrange
        String path = "testFolder";
        Path expectedPath = Paths.get(UPLOAD_DIR, path);
        Mockito.when(validationService.sanitizePath(path)).thenReturn(expectedPath.toString());

        // Act
        Path result = fileIOService.createDir(path);

        // Assert
        assertEquals(expectedPath, result);
        verify(validationService).sanitizePath(path);
        assertTrue(Files.exists(expectedPath));
    }

    @Test
    public void createDir_FolderExists() throws IOException {
        // Arrange
        String path = "testFolder";
        Path expectedPath = Paths.get(UPLOAD_DIR, path);
        Mockito.when(validationService.sanitizePath(path)).thenReturn(expectedPath.toString());

        // Act
        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
        () -> fileIOService.createDir(path));

        // Assert
        assertEquals("Folder already exists at this path.", exception.getMessage());
        verify(validationService).sanitizePath(path);
        assertTrue(Files.exists(expectedPath));

        //Clean up
        Files.deleteIfExists(expectedPath);
    }

    @Test
    public void testUploadFileNotAllowed() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello, World!".getBytes());
        Path uploadDir = Paths.get(UPLOAD_DIR, "testDir");
        String fileName = "test.txt";

        when(validationService.isFileAllowed(mockFile)).thenReturn(false);

        Assert.assertThrows(InvalidAttributesException.class, () -> fileIOService.uploadFile(mockFile, uploadDir, fileName));
    }

    @Test
    public void testGetFolderPathRootFolder() {
        RootFolder rootFolder = new RootFolder();
        rootFolder.setFolderNormalizedName("rootFolder");

        Path expectedPath = Paths.get(UPLOAD_DIR, "rootFolder");

        Path resultPath = fileIOService.getFolderPath(rootFolder);

        assertEquals(expectedPath, resultPath);
    }

    @Test
    public void testGetFolderPathSubFolder() {
        RootFolder rootFolder = new RootFolder();
        rootFolder.setFolderNormalizedName("rootFolder");
        SubFolder subFolder = new SubFolder();
        subFolder.setFolderNormalizedName("subFolder");
        subFolder.setParentFolder(rootFolder);

        Path expectedPath = Paths.get(UPLOAD_DIR, "rootFolder", "subFolder");

        Path resultPath = fileIOService.getFolderPath(subFolder);

        assertEquals(expectedPath, resultPath);
    }

    @Test
    public void testGetFilePath() {
        RootFolder rootFolder = new RootFolder();
        rootFolder.setFolderNormalizedName("rootFolder");
        FileMetaData file = new FileMetaData();
        file.setNormalizedFileName("file.txt");
        file.setFolder(rootFolder);
        Path expectedPath = Paths.get(UPLOAD_DIR, "rootFolder", "file.txt");

        Path resultPath = fileIOService.getFilePath(file);

        assertEquals(expectedPath, resultPath);
    }

    @Test
    public void testDeleteDir() throws IOException {
        Path dirPath = Files.createTempDirectory("testDir");

        assertTrue(fileIOService.deleteDir(dirPath));

        assertFalse(Files.exists(dirPath));
    }

    @Test
    public void testDeleteDirNotEmpty() throws IOException {
        Path dirPath = Files.createTempDirectory("testDir");
        Path filePath = Files.createFile(dirPath.resolve("file.txt"));

        assertThrows(DirectoryNotEmptyException.class, () -> fileIOService.deleteDir(dirPath));
    }

    @Test
    public void testDeleteFile() throws IOException {
        Path filePath = Files.createTempFile("testFile", ".txt");

        assertTrue(fileIOService.deleteFile(filePath));
        assertFalse(Files.exists(filePath));
    }
}
