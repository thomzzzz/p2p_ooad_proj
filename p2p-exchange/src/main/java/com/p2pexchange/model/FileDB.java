package com.p2pexchange.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Database entity for storing file metadata.
 * Actual file content is stored on disk.
 */
@Document(collection = "files")
public class FileDB {
    @Id
    private String id;
    private String filename;
    private String originalFilename;
    private String contentType;
    private long size;
    private String path;
    private String ownerId;
    private Date uploadDate;
    private String checksum;
    private String encryptionType;
    private Map<String, String> metadata = new HashMap<>();
    
    // Default constructor
    public FileDB() {
        this.uploadDate = new Date();
    }
    
    // Constructor with fields
    public FileDB(String filename, String originalFilename, String contentType, 
                long size, String path, String ownerId) {
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.path = path;
        this.ownerId = ownerId;
        this.uploadDate = new Date();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public String getOriginalFilename() {
        return originalFilename;
    }
    
    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public long getSize() {
        return size;
    }
    
    public void setSize(long size) {
        this.size = size;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    
    public Date getUploadDate() {
        return uploadDate;
    }
    
    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }
    
    public String getChecksum() {
        return checksum;
    }
    
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
    
    public String getEncryptionType() {
        return encryptionType;
    }
    
    public void setEncryptionType(String encryptionType) {
        this.encryptionType = encryptionType;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }
    
    // Convert this database entity to a model object
    public File toFileModel() {
        File file = new File(filename, path, contentType, size, ownerId);
        file.setId(id);
        file.setUploadDate(uploadDate);
        file.setMetadata(new HashMap<>(metadata));
        return file;
    }
}