package com.webserver.azure;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.*;

public class AzureBlobInterface {

    private static final String CONNECTION_STRING = System.getenv(
        "AZURE_STORAGE_CONNECTION_STRING"
    );
    private static final String CONTAINER_NAME = "userfiles";

    public static void uploadAndCreateEndpoints(
        int appID, // Assumes appID is globally unique, not just unique to each user
        String appName,
        InputStream zipStream
    ) {
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

                    String fileName = zipEntry
                        .getName()
                        .replaceFirst("^[^/]+", "");

                    // TODO: Create Endpoints

                    String blobName = appID + fileName;

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

    public static InputStream download(int appID, String filePath) {
        try {
            // Create the blob service client
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(CONNECTION_STRING)
                .buildClient();

            // Get container client
            BlobContainerClient containerClient =
                blobServiceClient.getBlobContainerClient(CONTAINER_NAME);

            // Make sure the container exists
            if (!containerClient.exists()) {
                System.err.println(
                    "Container doesn't exist: " + CONTAINER_NAME
                );
                return null;
            }

            // Calculate the full blob name using the same pattern from upload
            String blobName = appID + filePath;

            // Get the blob client
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            // Check if the blob exists
            if (!blobClient.exists()) {
                System.err.println("File doesn't exist: " + blobName);
                return null;
            }

            System.out.println("Downloading: " + blobName);

            // Download the blob and return as InputStream
            return blobClient.openInputStream();
        } catch (BlobStorageException ex) {
            System.err.println("Blob storage exception: " + ex.getMessage());
        } catch (Exception ex) {
            System.err.println("Exception: " + ex.getMessage());
        }

        return null;
    }

    public static void main(String[] args) {
        // Test upload
        int appID = 1;
        String appName = "MyCoolApp";
        System.out.println("Testing upload...");
        String filePath = "/home/dylan/sweng/test_azure/test_azure.zip";
        try (
            InputStream zipStream = Files.newInputStream(Paths.get(filePath))
        ) {
            uploadAndCreateEndpoints(appID, appName, zipStream);
        } catch (Exception e) {
            System.err.println("Exception: here " + e.getMessage());
        }

        // Test Download
        System.out.println("Testing download...");

        // Assuming there's a file inside the zip called "index.html"
        try (InputStream fileStream = download(appID, "/index.html")) {
            if (fileStream != null) {
                // Create output directory if it doesn't exist
                Path outputDir = Paths.get("downloaded_files");
                Files.createDirectories(outputDir);

                // Save file to disk
                Path outputFile = outputDir.resolve("index.html");
                Files.copy(
                    fileStream,
                    outputFile,
                    StandardCopyOption.REPLACE_EXISTING
                );

                System.out.println(
                    "Downloaded file saved to: " + outputFile.toAbsolutePath()
                );
                System.out.println(
                    "File size: " + Files.size(outputFile) + " bytes"
                );
            } else {
                System.out.println("Failed to download file");
            }
        } catch (Exception e) {
            System.err.println("Download test exception: " + e.getMessage());
        }
    }
}
