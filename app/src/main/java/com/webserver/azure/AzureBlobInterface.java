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

    private final BlobServiceClient blobServiceClient;
    private final BlobContainerClient containerClient;

    // Constructor initializes the clients
    public AzureBlobInterface() {
        this.blobServiceClient = new BlobServiceClientBuilder()
            .connectionString(CONNECTION_STRING)
            .buildClient();

        this.containerClient = blobServiceClient.getBlobContainerClient(
            CONTAINER_NAME
        );
        if (!containerClient.exists()) {
            containerClient.create();
        }
    }

    public void uploadAndCreateEndpoints(
        int appID, // Assumes appID is globally unique, not just unique to each user
        String appName,
        InputStream zipStream
    ) {
        try {
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

    public InputStream download(int appID, String filePath) {
        try {
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

    public int delete(int appID) {
        try {
            // Use the existing containerClient
            String prefix = String.valueOf(appID) + "/";

            // Counter for deleted files
            int deletedCount = 0;

            // List all blobs with the appID prefix
            System.out.println("Searching for files with prefix: " + prefix);

            // Use the listBlobs method to find all blobs with this app's prefix
            for (BlobItem blobItem : containerClient.listBlobs(
                new ListBlobsOptions().setPrefix(prefix),
                null
            )) {
                String blobName = blobItem.getName();
                System.out.println("Deleting: " + blobName);

                // Get the blob client and delete the blob
                containerClient.getBlobClient(blobName).delete();
                deletedCount++;
            }

            System.out.println(
                "Successfully deleted " +
                deletedCount +
                " files for appID: " +
                appID
            );
            return deletedCount;
        } catch (BlobStorageException ex) {
            System.err.println(
                "Blob storage exception during deletion: " + ex.getMessage()
            );
        } catch (Exception ex) {
            System.err.println("Exception during deletion: " + ex.getMessage());
        }

        return -1;
    }

    // For testing
    public static void main(String[] args) {
        AzureBlobInterface abi = new AzureBlobInterface();
        // Test upload
        int appID = -1;
        String appName = "MyCoolApp";
        System.out.println("Testing upload...");
        String filePath = "/home/dylan/sweng/test_azure/test_azure.zip";
        try (
            InputStream zipStream = Files.newInputStream(Paths.get(filePath))
        ) {
            abi.uploadAndCreateEndpoints(appID, appName, zipStream);
        } catch (Exception e) {
            System.err.println("Exception: here " + e.getMessage());
        }

        // Test Download
        System.out.println("Testing download...");

        // Assuming there's a file inside the zip called "index.html"
        try (InputStream fileStream = abi.download(appID, "/index.html")) {
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

        // Test Delete
        abi.delete(appID);
    }
}
