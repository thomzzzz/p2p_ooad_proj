package com.p2p.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "peers")
public class Peer {
    
    @Id
    private String id;
    private String userId;
    private String ipAddress;
    private int port;
    private boolean isOnline;
    private Date lastSeen;
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public boolean isOnline() {
        return isOnline;
    }
    
    public void setOnline(boolean online) {
        isOnline = online;
    }
    
    public Date getLastSeen() {
        return lastSeen;
    }
    
    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }
    
    // Additional methods based on activity diagrams
    public void joinRoom(Room room) {
        // Logic to join a room
    }
    
    public void leaveRoom(Room room) {
        // Logic to leave a room
    }
    
    public void sendFile(File file, Peer receiver) {
        // Logic to send a file to another peer
    }
    
    public void receiveFile(File file, Peer sender) {
        // Logic to receive a file from another peer
    }
}