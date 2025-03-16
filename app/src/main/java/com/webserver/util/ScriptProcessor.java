package com.webserver.util;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ScriptProcessor {
    public static final Map<String, String[]> SCRIPT_COMMANDS = new HashMap<>();
    static {
        SCRIPT_COMMANDS.put("php", new String[] {"php"});
        SCRIPT_COMMANDS.put("js", new String [] {"node"});
    }

    public static byte[] executeScript(String scriptPath)
    {
        String extension = getFileExtension(scriptPath);
        String [] command = SCRIPT_COMMANDS.get(extension);
        if (command == null)
        {
            return "can't execute command".getBytes();
        }
        try{
            ProcessBuilder processBuilder = new ProcessBuilder(command[0], scriptPath);
            processBuilder.directory(new File(scriptPath).getParentFile());
            Process process = processBuilder.start();

            InputStream inputStream = process.getInputStream();
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            process.waitFor();
            return result.toByteArray();
        }

        catch(Exception e) {
            return "An error occured".getBytes();
        }
    }

    //Returns the file extension
    public static String getFileExtension(String scriptPath)
    {
        int lastDotIndex = scriptPath.lastIndexOf('.');
        String fileExtension = lastDotIndex == -1 ?"" : scriptPath.substring(lastDotIndex+1);
        return fileExtension;
    }
}
