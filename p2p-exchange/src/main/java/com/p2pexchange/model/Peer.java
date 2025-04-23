package com.p2pexchange.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Represents a node in the P2P network.
 * A Peer is associated with a User and manages P2P connectivity details.
 */
@Document(collection = "peers")
public class Peer {
    @Id
    private String id;
    private String userId;
    private String ipAddress;
    private int port;
    private boolean online;
    private Date lastSeen;
    
    // Default constructor
    public Peer() {
        this.lastSeen = new Date();
        this.online = false;
    }
    
    // Constructor with fields
    public Peer(String userId, String ipAddress, int port) {
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.port = port;
        this.lastSeen = new Date();
        this.online = true;
    }
    
    // Status methods
    public void markOnline() {
        this.online = true;
        this.lastSeen = new Date();
    }
    
    public void markOffline() {
        this.online = false;
    }
    
    public boolean isActive() {
        // A peer is considered active if it was seen in the last 5 minutes
        if (!online) return false;
        
        Date now = new Date();
        long diffInMillis = now.getTime() - lastSeen.getTime();
        long diffInMinutes = diffInMillis / (60 * 1000);
        
        return diffInMinutes < 5;
    }
    
    // Connection string for direct P2P connections
    public String getConnectionString() {
        return ipAddress + ":" + port;
    }
    
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
        return online;
    }
    
    public void setOnline(boolean online) {
        this.online = online;
    }
    
    public Date getLastSeen() {
        return lastSeen;
    }
    
    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }
}