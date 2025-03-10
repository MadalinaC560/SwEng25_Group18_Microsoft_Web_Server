package com.webserver.util;

import java.io.IOException;
import java.nio.file.Path;

public class FileService {
    private final String webRoot;

    public FileService(String webRoot) {
        this.webRoot = webRoot;
    }

    public byte[] readFile(String path) throws IOException {
        // TODO: Implement secure file reading
        // 1. Validate and sanitize the path
        // 2. Prevent directory traversal attacks
        // 3. Read and return file contents
        // 4. Handle file not found and access errors
        return null;
    }

    public boolean isValidPath(String path) {
        // TODO: Implement path validation
        // 1. Check for directory traversal attempts
        // 2. Verify path is within webRoot
        // 3. Check file exists and is readable
        return false;
    }

    private Path resolvePath(String requestPath) {
        // TODO: Implement path resolution
        // 1. Convert request path to filesystem path
        // 2. Handle default files (e.g., index.html)
        // 3. Resolve relative to webRoot
        return null;
    }
}