
package com.webserver.azure;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AzureBlobInterface {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String CONNECTION_STRING = dotenv.get("AZURE_STORAGE_CONNECTION_STRING");
    private static final String CONTAINER_NAME = "userfiles";

    private final BlobServiceClient blobServiceClient;
    private final BlobContainerClient containerClient;

    public AzureBlobInterface() {
        this.blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(CONNECTION_STRING)
                .buildClient();

        this.containerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME);

        if (!containerClient.exists()) {
            containerClient.create();
        }
    }

    public List<String> upload(int appID, InputStream zipStream) {
        List<String> fileList = new ArrayList<>();
        try (ZipInputStream zipFile = new ZipInputStream(zipStream)) {
            ZipEntry zipEntry;

            while ((zipEntry = zipFile.getNextEntry()) != null) {

                // Skip directories
                if (zipEntry.isDirectory()) {
                    zipFile.closeEntry();
                    continue;
                }

                String entryName;

                String[] parts = zipEntry.getName().split("/", 2);
                // If there's more than 1 part, we skip the first
                if (parts.length > 1) {
                    entryName = parts[1];  // e.g. from "folderX/assets/css/styles.css" -> "assets/css/styles.css"
                } else {
                    entryName = parts[0];  // probably just a file at top level
                }

                String blobName = appID + "/" + entryName;

                fileList.add(entryName);

                // Create blob client
                BlobClient blobClient = containerClient.getBlobClient(blobName);

                // Get the uncompressed size
                long uncompressedSize = zipEntry.getSize();
                if (uncompressedSize < 0) {
                    uncompressedSize = 0;
                }

                // Upload directly from the zip stream
                blobClient.upload(zipFile, uncompressedSize, true);

                zipFile.closeEntry();
            }
        } catch (BlobStorageException ex) {
            System.err.println("Blob storage exception: " + ex.getMessage());
        } catch (IOException ex) {
            System.err.println("IOException during upload: " + ex.getMessage());
        } catch (Exception ex) {
            System.err.println("Exception during upload: " + ex.getMessage());
        }
        return fileList;
    }

    public InputStream download(int appID, String filePath) {
        try {
            // Strip leading slash, if any
            if (filePath.startsWith("/")) {
                filePath = filePath.substring(1);
            }
            // e.g. "index.html"
            String blobName = appID + "/" + filePath; // => "2002/index.html"

            BlobClient blobClient = containerClient.getBlobClient(blobName);

            if (!blobClient.exists()) {
                System.err.println("File doesn't exist in Azure: " + blobName);
                return null;
            }

            return blobClient.openInputStream();
        } catch (BlobStorageException ex) {
            System.err.println("Blob storage exception: " + ex.getMessage());
        } catch (Exception ex) {
            System.err.println("Exception in download: " + ex.getMessage());
        }
        return null;
    }

    public int delete(int appID) {
        try {
            int deletedCount = 0;
            String prefix = appID + "/";  // e.g. "2002/"

            for (BlobItem blobItem : containerClient.listBlobs(
                    new ListBlobsOptions().setPrefix(prefix),
                    null
            )) {
                String blobName = blobItem.getName();
                containerClient.getBlobClient(blobName).delete();
                deletedCount++;
            }
            return deletedCount;
        } catch (BlobStorageException ex) {
            System.err.println("Blob storage exception during deletion: " + ex.getMessage());
        } catch (Exception ex) {
            System.err.println("Exception during deletion: " + ex.getMessage());
        }
        return -1;
    }

    public List<String> test_upload(int appID) {
        System.out.println("Testing upload for appID=" + appID);
        // Adjust path if your test file is somewhere else
        String filePath = System.getProperty("user.dir") + "/test_azure/test_azure.zip";
        try (InputStream zipStream = Files.newInputStream(Paths.get(filePath))) {
            return upload(appID, zipStream);
        } catch (Exception e) {
            System.err.println("Exception in test_upload: " + e.getMessage());
            return null;
        }
    }

    public void test_download(int appID) {
        System.out.println("Testing download for appID=" + appID);
        try (InputStream fileStream = download(appID, "/index.html")) {
            if (fileStream == null) {
                System.out.println("File not found or error in download");
                return;
            }
            Path outputDir = Paths.get("downloaded_files");
            Files.createDirectories(outputDir);
            Path outputFile = outputDir.resolve("index.html");
            Files.copy(fileStream, outputFile, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Downloaded file to: " + outputFile.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Exception in test_download: " + e.getMessage());
        }
    }

    // TODO: Migrate this to unit tests
    // For testing
    public void test() {
        int appID = 2002;
        test_upload(appID);
        test_download(appID);
        //delete(appID);
    }
}
