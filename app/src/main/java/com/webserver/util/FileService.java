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

    public byte[] readFile(String requestPath) throws IOException {
        Path resolvedPath = resolvePath(requestPath);
        if (!isValidPath(resolvedPath))
        {
                throw new IOException("Not a valid path" + requestPath);
        }
        File file = new File(resolvedPath.toString());
        if (!file.exists() || !file.isFile()) {
            throw new IOException("File not found" + requestPath);
        }
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

    public boolean isValidPath(Path path) {
        try {
            // Check for . or .. in the string
            String absoluteString = path.toString();
            if (absoluteString.contains("..") || absoluteString.contains("./") || absoluteString.contains("/.")) {
                return false;
            }

            // Must start with webroot
            Path webRootPath = Paths.get(webRoot).toAbsolutePath().normalize();
            if (!path.startsWith(webRootPath)) {
                return false;
            }

            // Must exist, be a file, and be readable
            if (!Files.exists(path) || !Files.isRegularFile(path) || !Files.isReadable(path)) {
                return false;
            }

            return true;
        } catch (SecurityException | InvalidPathException e) {
            return false;
        }
    }


    private Path resolvePath(String requestPath) {
        requestPath = requestPath.replaceAll("^/+", "");

        if (requestPath.isEmpty() || requestPath.endsWith("/"))
        {
            requestPath += "index.html";
        }
        Path webRootPath = Paths.get(webRoot).toAbsolutePath();
        Path resolvedPath = webRootPath.resolve(requestPath).normalize();
        return resolvedPath;
    }
}