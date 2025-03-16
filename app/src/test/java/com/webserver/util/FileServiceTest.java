package com.webserver.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FileServiceTest {
    private static final String TEST_WEBROOT = "test_webroot";
    private FileService fileService;

    @BeforeEach
    void setUp() throws IOException {
        //  Create a test directory
        Files.createDirectories(Paths.get(TEST_WEBROOT));
        Files.write(Paths.get(TEST_WEBROOT, "testfile.txt"), "FileService Test".getBytes());
        fileService = new FileService(TEST_WEBROOT);
    }

    @Test
    void testReadFileSuccess() throws IOException {
        byte[] fileData = fileService.readFile("/testfile.txt");
        assertEquals("FileService Test", new String(fileData));
    }

    @Test
    void testReadFileNotFound() {
        assertThrows(IOException.class, () -> fileService.readFile("/doesnotexist.txt"));
    }

    @Test
    void testPreventDirectoryTraversal() {
        assertFalse(fileService.isValidPath("/../etc/passwd"));
    }

    @Test
    void testValidPath() {
        assertTrue(fileService.isValidPath("/testfile.txt"));
    }
}
