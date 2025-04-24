package com.p2p.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Document(collection = "rooms")
public class Room {
    
    @Id
    private String id;
    private String name;
    private String creatorId;
    private List<String> members = new ArrayList<>();
    private List<String> files = new ArrayList<>();
    private Date createdAt;
    private String roomLink;
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }
    
    public List<String> getMembers() {
        return members;
    }
    
    public void setMembers(List<String> members) {
        this.members = members;
    }
    
    public List<String> getFiles() {
        return files;
    }
    
    public void setFiles(List<String> files) {
        this.files = files;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getRoomLink() {
        return roomLink;
    }
    
    public void setRoomLink(String roomLink) {
        this.roomLink = roomLink;
    }
    
    // Additional methods
    public void addMember(String userId) {
        if (!members.contains(userId)) {
            members.add(userId);
        }
    }
    
    public void removeMember(String userId) {
        members.remove(userId);
    }
    
    public void addFile(String fileId) {
        if (!files.contains(fileId)) {
            files.add(fileId);
        }
    }
    
    public void removeFile(String fileId) {
        files.remove(fileId);
    }
    
    public String generateLink() {
        // Logic to generate a unique link
        this.roomLink = UUID.randomUUID().toString();
        return this.roomLink;
    }
}