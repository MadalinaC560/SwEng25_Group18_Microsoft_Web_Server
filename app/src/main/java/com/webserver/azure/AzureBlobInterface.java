package com.webserver.azure;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.*;

public class AzureBlobInterface {

    private static final String CONNECTION_STRING = System.getenv(
        "AZURE_STORAGE_CONNECTION_STRING"
    );
    private static final String CONTAINER_NAME = "userfiles";

    public static void upload(InputStream zipStream) {
        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(CONNECTION_STRING)
                .buildClient();

            BlobContainerClient containerClient =
                blobServiceClient.getBlobContainerClient(CONTAINER_NAME);

            if (!containerClient.exists()) {
                containerClient.create();
            }

            try (ZipInputStream zipFile = new ZipInputStream(zipStream)) {
                ZipEntry zipEntry;

                while ((zipEntry = zipFile.getNextEntry()) != null) {
                    if (zipEntry.isDirectory()) {
                        zipFile.closeEntry();
                        continue;
                    }

                    String blobName = zipEntry.getName();
                    BlobClient blobClient = containerClient.getBlobClient(
                        blobName
                    );

                    System.out.println("Uploading: " + blobName);

                    // Upload directly from ZipInputStream
                    blobClient.upload(zipFile, zipEntry.getSize(), true);

                    System.out.println("Uploaded: " + blobName);
                    zipFile.closeEntry();
                }
            }
        } catch (BlobStorageException ex) {
            System.err.println("Blob storage exception: " + ex.getMessage());
        } catch (Exception ex) {
            System.err.println("Exception: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        String filePath = "/home/dylan/sweng/test_azure/test_azure.zip";
        try (
            InputStream zipStream = Files.newInputStream(Paths.get(filePath))
        ) {
            upload(zipStream);
        } catch (Exception e) {
            System.err.println("Exception: here " + e.getMessage());
        }
    }
}
