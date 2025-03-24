package com.webserver.azure;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import com.webserver.http.RequestProcessor;
import com.webserver.model.HttpResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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

    // Returns a list of endpoints to be made.
    // Each endpoint should call `download()` with the appropriate appID and filePath
    public ArrayList<String> upload(
        int appID, // Assumes appID is globally unique, not just unique to each user
        InputStream zipStream
    ) {
        ArrayList<String> fileList = new ArrayList<String>();
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
                        .replaceFirst("^[^/]+", "")
                        .substring(1);

                    fileList.add(fileName);

                    String blobName = appID + fileName;

                    BlobClient blobClient = containerClient.getBlobClient(
                        blobName
                    );
                    System.out.println("Uploading: " + blobName);

                    System.out.println("Zipentrysize: " + zipEntry.getSize());

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

        return fileList;
    }

    public InputStream download(int appID, String filePath) {
        System.out.println("Downloading " + filePath + " from Azure");
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

    // TODO: Make sure to delete endpoints as well
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

    public ArrayList<String> test_upload(int appID) {
        // Test upload
        System.out.println("Testing upload...");

        String filePath = System.getProperty("user.dir") + "/test_azure/test_azure.zip";
        System.out.println("Uploaded file path: " + filePath);
        try (
            InputStream zipStream = Files.newInputStream(Paths.get(filePath))
        ) {
            return upload(appID, zipStream);
        } catch (Exception e) {
            System.err.println("Exception: here " + e.getMessage());
            return null;
        }
    }

    public void test_download(int appID) {
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

    // TODO: Migrate this to unit tests
    // For testing
    public void test() {
        int appID = -1;
        test_upload(appID);

        test_download(appID);

        delete(appID);
    }
}
