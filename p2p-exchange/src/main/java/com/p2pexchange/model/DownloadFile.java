package com.p2pexchange.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * Represents a file being downloaded from the system.
 * Handles the download process and validation.
 */
public class DownloadFile {
    private String uploadLocation;
    private String filePath;
    private long fileSize;
    private long downloadSpeed;
    
    // Default constructor
    public DownloadFile() {
    }
    
    // Constructor with file path
    public DownloadFile(String filePath, String uploadLocation) {
        this.filePath = filePath;
        this.uploadLocation = uploadLocation;
        try {
            Path path = Paths.get(filePath);
            this.fileSize = Files.size(path);
        } catch (IOException e) {
            this.fileSize = 0;
        }
    }
    
    // Methods
    public Resource getAsResource() throws IOException {
        Path path = Paths.get(filePath);
        Resource resource = new UrlResource(path.toUri());
        
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new IOException("Could not read file: " + filePath);
        }
    }
    
    public String calculateChecksum() throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        Path path = Paths.get(filePath);
        byte[] bytes = Files.readAllBytes(path);
        byte[] digest = md.digest(bytes);
        
        // Convert to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
    
    public boolean validateChecksum(String expectedChecksum) throws IOException, NoSuchAlgorithmException {
        String actualChecksum = calculateChecksum();
        return actualChecksum.equals(expectedChecksum);
    }
    
    public String getContentType() throws IOException {
        Path path = Paths.get(filePath);
        return Files.probeContentType(path);
    }
    
    // Getters and Setters
    public String getUploadLocation() {
        return uploadLocation;
    }
    
    public void setUploadLocation(String uploadLocation) {
        this.uploadLocation = uploadLocation;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
        try {
            Path path = Paths.get(filePath);
            this.fileSize = Files.size(path);
        } catch (IOException e) {
            this.fileSize = 0;
        }
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public long getDownloadSpeed() {
        return downloadSpeed;
    }
    
    public void setDownloadSpeed(long downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }
    
    // Get filename from path
    public String getFilename() {
        Path path = Paths.get(filePath);
        return path.getFileName().toString();
    }
    
    // Get file data as bytes
    public byte[] getFileBytes() throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }
}