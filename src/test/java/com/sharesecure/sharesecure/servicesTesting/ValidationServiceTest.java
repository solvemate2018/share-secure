package com.sharesecure.sharesecure.servicesTesting;

import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.mock.web.MockMultipartFile;

import com.sharesecure.sharesecure.services.utils.validation.ValidationService;

public class ValidationServiceTest {
    private final ValidationService validationService = new ValidationService();

    @ParameterizedTest
    @MethodSource("sanitizeFileNameDataProvider")
    public void testSanitizeFileName(String originalFileName, String expectedResult){
        String sanitizedFileName = validationService.sanitizeFileName(originalFileName);

        Assert.assertEquals(expectedResult, sanitizedFileName);
    }

    private static Stream<Arguments> sanitizeFileNameDataProvider() {
        return Stream.of(
                Arguments.of("CON.txt", "CON.txt"),
                Arguments.of("file_with_dot.txt", "file_with_dot.txt"),
                Arguments.of("file with space.txt", "file_with_space.txt"),
                Arguments.of("file<with>reserved:characters.txt", "file_with_reserved_characters.txt"),
                Arguments.of(".hidden_file.txt", "_hidden_file.txt"),
                Arguments.of("file@with#special$characters.txt", "file_with_special_characters.txt"),
                Arguments.of("../parent_directory/file.txt", "___parent_directory_file.txt"),
                Arguments.of("file_with_question?.txt", "file_with_question_.txt"),
                Arguments.of("file_with_asterisk*.txt", "file_with_asterisk_.txt"),
                Arguments.of("MixedCaseFile.txt", "MixedCaseFile.txt")
        );
    }

    @ParameterizedTest
    @MethodSource("sanitizePathDataProvider")
    public void testSanitizePath(String originalPath, String expectedResult) {
        String sanitizedPath = validationService.sanitizePath(originalPath);
        Assert.assertEquals(expectedResult, sanitizedPath);
    }

    private static Stream<Arguments> sanitizePathDataProvider() {
        return Stream.of(
                Arguments.of("relative/path/to/file.txt", "C:\\Users\\georg\\Desktop\\EASV Exams 1st semester\\Software Sec\\share-secure\\storage\\users\\relative\\path\\to\\file.txt"),
                Arguments.of("C:\\Users\\georg\\Desktop\\EASV Exams 1st semester\\Software Sec\\share-secure\\storage\\users\\file.txt", "C:\\Users\\georg\\Desktop\\EASV Exams 1st semester\\Software Sec\\share-secure\\storage\\users\\file.txt"),
                Arguments.of("../parent_directory/file.txt", "C:\\Users\\georg\\Desktop\\EASV Exams 1st semester\\Software Sec\\share-secure\\storage\\parent_directory\\file.txt"),
                Arguments.of("folder/with/spaces  ", "C:\\Users\\georg\\Desktop\\EASV Exams 1st semester\\Software Sec\\share-secure\\storage\\users\\folder\\with\\spaces")
        );
    }

    @ParameterizedTest
    @MethodSource("sanitizeEmailDataProvider")
    public void testSanitizeEmail(String originalEmail, String expectedResult) {
        String sanitizedEmail = validationService.sanitizeEmail(originalEmail);
        Assert.assertEquals(expectedResult, sanitizedEmail);
    }

    private static Stream<Arguments> sanitizeEmailDataProvider() {
        return Stream.of(
                Arguments.of("user@example.com", "userexample.com"),
                Arguments.of("User_Name@domain.com", "user_namedomain.com"),
                Arguments.of("user123@example.com", "user123example.com"),
                Arguments.of("user!@#%^.name@example.com", "user.nameexample.com")
        );
    }

    @Test
    public void testIsFileAllowed() {
        MockMultipartFile mockFile = new MockMultipartFile("file", "image.jpeg", "image/jpeg", "Hello, World!".getBytes());

        Assert.assertTrue(validationService.isFileAllowed(mockFile));
    }

    @Test
    public void testIsFileNotAllowed() {
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.exe", "application/octet-stream", "Hello, World!".getBytes());

        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> validationService.isFileAllowed(mockFile));

                Assert.assertEquals("Invalid file type. Allowed types are: image/jpeg, image/png, application/pdf", exception.getMessage());
    }

    @Test
    public void testIsFileTooBig() {
        MockMultipartFile mockFile = new MockMultipartFile("file", "image.jpeg", "image/jpeg", new byte[6 * 1024 * 1024]);

        IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> validationService.isFileAllowed(mockFile));

                Assert.assertEquals("File size exceeds the maximum allowed size.", exception.getMessage());
    }

    @Test
    public void testIsFileTypeAllowed() {
        String[] allowedFileTypes = {"image/jpeg", "image/png", "application/pdf"};
        Assert.assertTrue(validationService.isFileTypeAllowed("image/jpeg", allowedFileTypes));
    }

    @Test
    public void testIsFileTypeNotAllowed() {
        String[] allowedFileTypes = {"image/jpeg", "image/png", "application/pdf"};
        Assert.assertFalse(validationService.isFileTypeAllowed("application/octet-stream", allowedFileTypes));
    }
}
