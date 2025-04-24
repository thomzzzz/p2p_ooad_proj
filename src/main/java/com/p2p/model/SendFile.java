package com.p2p.model;

public class SendFile {
    
    private String clientId;
    private File file;
    
    // Getters and Setters
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public File getFile() {
        return file;
    }
    
    public void setFile(File file) {
        this.file = file;
    }
    
    // Methods
    public void encryptFile(File file) {
        // Logic to encrypt the file using Crypto service
    }
    
    public File getEncryptedFile() {
        return this.file;
    }
    
    public byte[] serialize(File file) {
        // Logic to serialize file for transfer
        return null; // Placeholder
    }
}