package com.p2pexchange.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ReceiveFile {
    private String source;          // Client ID of sender
    private File receivedFile;      // The received file
    private byte[] encryptedData;   // Encrypted file data
    private boolean validated;      // Whether the file has been validated
    private String expectedChecksum; // Expected checksum from sender
    
    // Default constructor
    public ReceiveFile() {
    }
    
    // Constructor with source
    public ReceiveFile(String source) {
        this.source = source;
        this.validated = false;
    }
    
    // Methods
    public boolean decryptAndSave(byte[] encryptedData, String decryptionKey, String targetPath, String filename) {
        try {
            this.encryptedData = encryptedData;
            
            // In a real implementation, this would decrypt the data
            byte[] decryptedData = encryptedData;  // Placeholder
            
            // Create target directory if it doesn't exist
            Path directory = Paths.get(targetPath);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            
            // Save the file
            Path filePath = Paths.get(targetPath, filename);
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                fos.write(decryptedData);
            }
            
            receivedFile = filePath.toFile();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean validateChecksum() {
        if (receivedFile == null || expectedChecksum == null) {
            return false;
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(receivedFile.toPath());
            byte[] digest = md.digest(fileBytes);
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            String calculatedChecksum = hexString.toString();
            validated = calculatedChecksum.equals(expectedChecksum);
            return validated;
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Getters and Setters
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public File getReceivedFile() {
        return receivedFile;
    }
    
    public void setReceivedFile(File receivedFile) {
        this.receivedFile = receivedFile;
    }
    
    public byte[] getEncryptedData() {
        return encryptedData;
    }
    
    public void setEncryptedData(byte[] encryptedData) {
        this.encryptedData = encryptedData;
    }
    
    public boolean isValidated() {
        return validated;
    }
    
    public void setValidated(boolean validated) {
        this.validated = validated;
    }
    
    public String getExpectedChecksum() {
        return expectedChecksum;
    }
    
    public void setExpectedChecksum(String expectedChecksum) {
        this.expectedChecksum = expectedChecksum;
    }
}