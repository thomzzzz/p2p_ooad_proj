package com.p2pexchange.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.web.multipart.MultipartFile;

/**
 * Represents a file being uploaded to the system.
 * Handles the upload process, validation, and preparation for storage.
 */
public class UploadFile {
    private String uploadLocation;
    private long fileSize;
    private long uploadSpeed;
    private MultipartFile file;
    
    // Default constructor
    public UploadFile() {
    }
    
    // Constructor with MultipartFile
    public UploadFile(MultipartFile file, String uploadLocation) {
        this.file = file;
        this.uploadLocation = uploadLocation;
        this.fileSize = file.getSize();
    }
    
    // Methods
    public String upload() throws IOException {
        Path directory = Paths.get(uploadLocation);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        
        // Generate unique filename to avoid collisions
        String uniqueFilename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path targetPath = directory.resolve(uniqueFilename);
        
        // Save the file
        file.transferTo(targetPath);
        
        return targetPath.toString();
    }
    
    public String calculateChecksum() throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = file.getBytes();
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
    
    public boolean validateFileType() {
        String contentType = file.getContentType();
        
        // Reject null content types
        if (contentType == null) {
            return false;
        }
        
        // Add more content type validations as needed
        // For now, we'll accept common file types
        return contentType.startsWith("image/") ||
               contentType.startsWith("text/") ||
               contentType.startsWith("application/pdf") ||
               contentType.startsWith("application/msword") ||
               contentType.startsWith("application/vnd.openxmlformats-officedocument") ||
               contentType.startsWith("application/vnd.ms-excel") ||
               contentType.startsWith("application/vnd.ms-powerpoint") ||
               contentType.startsWith("application/zip");
    }
    
    public boolean validateFileSize(long maxSizeBytes) {
        return file.getSize() <= maxSizeBytes;
    }
    
    // Getters and Setters
    public String getUploadLocation() {
        return uploadLocation;
    }
    
    public void setUploadLocation(String uploadLocation) {
        this.uploadLocation = uploadLocation;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public long getUploadSpeed() {
        return uploadSpeed;
    }
    
    public void setUploadSpeed(long uploadSpeed) {
        this.uploadSpeed = uploadSpeed;
    }
    
    public MultipartFile getFile() {
        return file;
    }
    
    public void setFile(MultipartFile file) {
        this.file = file;
        this.fileSize = file.getSize();
    }
    
    // Get file details
    public String getOriginalFilename() {
        return file.getOriginalFilename();
    }
    
    public String getContentType() {
        return file.getContentType();
    }
    
    public byte[] getFileBytes() throws IOException {
        return file.getBytes();
    }
}