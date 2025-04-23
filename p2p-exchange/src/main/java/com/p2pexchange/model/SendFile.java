package com.p2pexchange.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SendFile {
    private String destination;  // Client ID
    private File file;
    
    // Default constructor
    public SendFile() {
    }
    
    // Constructor with fields
    public SendFile(String destination, File file) {
        this.destination = destination;
        this.file = file;
    }
    
    // Methods
    public byte[] prepareFile() throws IOException {
        return Files.readAllBytes(file.toPath());
    }
    
    public byte[] encryptFile(byte[] fileData, String encryptionKey) {
        // Implementation would depend on the crypto service
        // This is just a placeholder
        return fileData;  // In a real implementation, this would return encrypted data
    }
    
    public String calculateChecksum() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }
            byte[] digest = md.digest();
            
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
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Getters and Setters
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public File getFile() {
        return file;
    }
    
    public void setFile(File file) {
        this.file = file;
    }
}