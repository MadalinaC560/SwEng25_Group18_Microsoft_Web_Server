package com.webserver.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class FileService {
    private final String webRoot;

    public FileService(String webRoot) {
        this.webRoot = webRoot;
    }

    // Reads a file's content and returns it as a byte array
    // Ensures the file is within the webRoots directiory
    public byte[] readFile(String path) throws IOException {
        // TODO: Implement secure file reading
        // 1. Validate and sanitize the path
        // 2. Prevent directory traversal attacks
        // 3. Read and return file contents
        // 4. Handle file not found and access errors

        Path resolvedPath = resolvePath(path);
        if(!isValidPath(path))
        {
            throw new IOException("Invalid path: " + path);
        }
        return Files.readAllBytes((resolvedPath));
    }

    public boolean isValidPath(String path) {
        // 1. Check for directory traversal attempts
        // 2. Verify path is within webRoot
        // 3. Check file exists and is readable

        Path resolvedPath = resolvePath(path);

        if (!resolvedPath.startsWith(Paths.get(webRoot))) {
            return false;
        }

        return Files.exists(resolvedPath) && !Files.isDirectory(resolvedPath);

    }

    private Path resolvePath(String requestPath) {
        // 1. Convert request path to filesystem path
        // 2. Handle default files (e.g., index.html)
        // 3. Resolve relative to webRoot

        if (requestPath.equals("/")) {
            requestPath = "index.html"; // Serve index.html by default
        }
        return Paths.get(webRoot).resolve(requestPath.substring(1)).normalize();
    }

    public String getFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }
    public boolean fileExists(String path) {
        Path filePath = resolvePath(path);
        return Files.exists(filePath) && Files.isRegularFile(filePath);
    }
    



}