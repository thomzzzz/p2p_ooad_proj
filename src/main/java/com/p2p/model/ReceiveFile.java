package com.p2p.model;

public class ReceiveFile {
    
    private String clientId;
    private File receivedFile;
    private boolean validated;
    
    // Getters and Setters
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public File getReceivedFile() {
        return receivedFile;
    }
    
    public void setReceivedFile(File receivedFile) {
        this.receivedFile = receivedFile;
    }
    
    public boolean isValidated() {
        return validated;
    }
    
    public void setValidated(boolean validated) {
        this.validated = validated;
    }
    
    // Methods
    public File decrypt(File encryptedFile, String key) {
        // Logic to decrypt file
        return null; // Placeholder
    }
    
    public boolean validateFile(File file) {
        // Logic to validate the integrity of the file
        this.validated = true; // Placeholder
        return this.validated;
    }
    
    public File getFile() {
        if (this.validated) {
            return this.receivedFile;
        }
        return null;
    }
}