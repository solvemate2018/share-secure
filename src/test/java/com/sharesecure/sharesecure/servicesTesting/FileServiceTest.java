package com.sharesecure.sharesecure.servicesTesting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.naming.directory.InvalidAttributesException;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.sharesecure.sharesecure.entities.FileMetaData;
import com.sharesecure.sharesecure.entities.User;
import com.sharesecure.sharesecure.entities.UserPrivilege;
import com.sharesecure.sharesecure.entities.enums.PrivilegeType;
import com.sharesecure.sharesecure.entities.folder.Folder;
import com.sharesecure.sharesecure.entities.folder.RootFolder;
import com.sharesecure.sharesecure.repositories.FileMetaDataRepo;
import com.sharesecure.sharesecure.repositories.FolderRepo;
import com.sharesecure.sharesecure.services.files.FileService;
import com.sharesecure.sharesecure.services.utils.fileio.FileIOServiceInterface;
import com.sharesecure.sharesecure.services.utils.validation.ValidationServiceInterface;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class FileServiceTest {
    @Mock
    private FileMetaDataRepo fileMetaDataRepo;

    @Mock
    private FolderRepo folderRepo;

    @Mock
    private ValidationServiceInterface validationService;

    @Mock
    private FileIOServiceInterface fileIOService;

    @InjectMocks
    private FileService fileService;
 
    private Long fileId;

    private User testedUser;

    private RootFolder testedRootFolder;

    private FileMetaData testedFile;

    private final String UPLOAD_DIR = getApplicationLocation() + "\\storage\\users";

    public String getApplicationLocation() {
        ApplicationHome home = new ApplicationHome(getClass());
        
        return home.getSource().getAbsolutePath().substring(0, home.getSource().getAbsolutePath().length() - 15);
    }

    @Before
    public void setUp() throws IOException{
        // Mock data
        fileId = 1L;
        User user = new User(); // Mock a real user object
        user.setEmail("test-email@test.com");
        user.setFirstName("Test");
        user.setLastName("Test");
        user.setPassword("test123");

        Long rootFolderId = 1L;
        RootFolder rootFolder = new RootFolder();
        rootFolder.setFolderOwner(user);
        rootFolder.setFolderNormalizedName("normal_name");
        user.setRootFolder(rootFolder);
        rootFolder.setId(rootFolderId);

        // Mock file
        FileMetaData file = new FileMetaData();
        file.setId(fileId);
        file.setFolder(rootFolder);
        file.setFileName("test.txt");
        file.setNormalizedFileName("test.txt");
        rootFolder.addFile(file);

        UserPrivilege privilege = new UserPrivilege();
        privilege.setFile(file);
        privilege.setOwner(true);
        privilege.setUser(user);
        privilege.setUserPrivileges(Arrays.asList(PrivilegeType.values()));
        file.addUserPrivilege(privilege);

        testedUser = user;
        testedRootFolder = rootFolder;
        testedFile = file;
    }

    @Test
    public void testUploadFile() throws IOException, InvalidAttributesException {
        // Mock data
        MultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello, World!".getBytes());

        when(folderRepo.existsById(testedRootFolder.getId())).thenReturn(true);
        when(folderRepo.getReferenceById(testedRootFolder.getId())).thenReturn(testedRootFolder);
        when(fileIOService.getFolderPath(testedRootFolder)).thenReturn(Paths.get(UPLOAD_DIR, "/normal_name"));

        Files.createDirectories(fileIOService.getFolderPath(testedRootFolder));
        // Perform the uploadFile operation
        Folder resultFolder = fileService.uploadFile(testedRootFolder.getId(), mockFile, testedUser);

        // Verify the interactions
        verify(folderRepo, times(1)).existsById(testedRootFolder.getId());
        verify(folderRepo, times(1)).getReferenceById(testedRootFolder.getId());
        verify(fileIOService, times(3)).getFolderPath(testedRootFolder);
        verify(fileMetaDataRepo, times(1)).save(any(FileMetaData.class));

        // Assertions
        assertNotNull(resultFolder);
        assertEquals(resultFolder, testedRootFolder);

        //Clean up
        Files.deleteIfExists(fileIOService.getFolderPath(testedRootFolder));
    }
    

    @Test
    public void testDeleteFile() throws InvalidAttributesException, IOException {
        Path filePath = Paths.get(UPLOAD_DIR, "/normal_name/test.txt");
        when(fileMetaDataRepo.existsById(fileId)).thenReturn(true);
        when(fileMetaDataRepo.getReferenceById(fileId)).thenReturn(testedFile);
        when(fileIOService.getFilePath(testedFile)).thenReturn(filePath);
        when(fileIOService.deleteFile(filePath)).thenReturn(true);

        Files.createDirectories(Paths.get(UPLOAD_DIR, "/normal_name"));

        // Perform the deleteFile operation
        boolean result = fileService.deleteFile(fileId, testedUser);

        // Verify the interactions
        verify(fileMetaDataRepo, times(1)).existsById(fileId);
        verify(fileMetaDataRepo, times(1)).getReferenceById(fileId);
        verify(fileIOService, times(1)).getFilePath(testedFile);

        // Assertions
        assertTrue(result);

        // Clean up
        Files.deleteIfExists(Paths.get(UPLOAD_DIR, "/normal_name"));
    }

}