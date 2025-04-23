package com.p2pexchange.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "files")
public class File {
    @Id
    private String id;
    private String filename;
    private String location;
    private String fileType;
    private long fileSize;
    private String owner;
    private Date uploadDate;
    private Map<String, String> metadata = new HashMap<>();
    
    // Default constructor
    public File() {
        this.uploadDate = new Date();
    }
    
    // Constructor with fields
    public File(String filename, String location, String fileType, long fileSize, String owner) {
        this.filename = filename;
        this.location = location;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.owner = owner;
        this.uploadDate = new Date();
    }
    
    // Methods
    public void addMetadata(String key, String value) {
        metadata.put(key, value);
    }
    
    public String getMetadata(String key) {
        return metadata.get(key);
    }
    
    public void removeMetadata(String key) {
        metadata.remove(key);
    }
    
    public boolean delete() {
        // Implementation to delete the actual file
        // This would typically involve file system operations
        return true;
    }
    
    public String getOwner() {
        return owner;
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
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public void setOwner(String owner) {
        this.owner = owner;
    }
    
    public Date getUploadDate() {
        return uploadDate;
    }
    
    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}