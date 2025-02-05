package org.example.app;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHandler {
    private final File webRoot; //files to serve will be stored in the webRoot directory

    public FileHandler(String webRootPath)
    {
        this.webRoot = new File(webRootPath);
        if (!webRoot.exists() || !webRoot.isDirectory())
        {
            System.err.print("webRoot is not a directory");
        }
    }

    //Reads a file and returns its contents as a byte array
    //throws an IOException if the file does not exist or can't be read
    //still need to add more checks to test if the filepat is properly formed
    public byte[] readfile(String filepath) throws IOException
    {

        File file = new File(webRoot, filepath);
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

    //Determines the mime type of a file
    //uses the build in java method Files.probeContentType
    private String getMimeType(String pathToFile)
    {
        Path path = Paths.get(pathToFile);
        try
        {
            String mimeType = Files.probeContentType(path);
            if (mimeType != null)
            {
                return mimeType;
            }
            else
            {
                return "application/octet-stream";
            }
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}

