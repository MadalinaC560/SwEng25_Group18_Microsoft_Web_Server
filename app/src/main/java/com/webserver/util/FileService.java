package com.webserver.util;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileInputStream;

public class FileService {
    private final String webRoot;

    public FileService(String webRoot) {
        this.webRoot = webRoot;
    }

    public byte[] readFile(String path) throws IOException {
        // TODO: Implement secure file reading
        // 1. Validate and sanitize the path
        // 2. Prevent directory traversal attacks
        Path resolvedPath = resolvePath(path);
        if (!isValidPath(resolvedPath.toString()))
        {
                throw new IOException("Not a valid path" + path);
        }
        // 4. Handle file not found and access errors
        File file = new File(resolvedPath.toString());
        if (!file.exists() || !file.isFile()) {
            throw new IOException("File not found" + path);
        }
        // 3. Read and return file contents
        FileInputStream fileInputStream = new FileInputStream(file);
        byte [] fileBytes = new byte[(int)file.length()];
        try
        {
            fileInputStream.read(fileBytes);
            fileInputStream.close();
        }
        catch (IOException e)
        {
            throw e;
        }
        return fileBytes;
    }

    public boolean isValidPath(String path) {
        // TODO: Implement path validation
        try{
            Path resolvedPath = resolvePath(path);
            Path webRootPath = Paths.get(webRoot).toAbsolutePath().normalize();
            // 1. Check for directory traversal attempts
            String resolvedPathString = resolvedPath.toString();
            if (resolvedPathString.contains("..") || resolvedPathString.contains("./") || resolvedPathString.contains("/.")) {
                return false;
            }
            // 2. Verify path is within webRoot
            if (!resolvedPath.startsWith(webRootPath)){
                return false;
            }
            // 3. Check file exists and is readable
            if (!Files.exists(resolvedPath) ||
                    !Files.isRegularFile(resolvedPath) ||
                    !Files.isReadable(resolvedPath)) {
                return false;
            }
            return true;
        }
        catch (SecurityException | InvalidPathException e )
        {
            return false;
        }
    }

    private Path resolvePath(String requestPath) {
        // TODO: Implement path resolution
        // 1. Convert request path to filesystem path
        //Remove forward slashes at the start of the string
        requestPath = requestPath.replaceAll("^/+", "");

        // 2. Handle default files (e.g., index.html)
        //Set default index file if no path requested
        if (requestPath.isEmpty() || requestPath.endsWith("/"))
        {
            requestPath += "index.html";
        }
        // 3. Resolve relative to webRoot
        Path webRootPath = Paths.get(webRoot).toAbsolutePath();
        Path resolvedPath = webRootPath.resolve(requestPath).normalize();
        return resolvedPath;
    }
    public static String getFileExtension(String scriptPath) {
        int lastDotIndex = scriptPath.lastIndexOf('.');
        String fileExtension = lastDotIndex == -1 ?"" : scriptPath.substring(lastDotIndex+1);
        return fileExtension;
    }


}
