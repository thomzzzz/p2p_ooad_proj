package com.p2pexchange.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "clients")
public class Client {
    @Id
    private String id;
    private String ipAddress;
    private String username;
    private Set<Room> rooms = new HashSet<>();
    private String activeRoomId;
    private String peerId;
    
    // Default constructor
    public Client() {
    }
    
    // Constructor with fields
    public Client(String ipAddress, String username) {
        this.ipAddress = ipAddress;
        this.username = username;
    }
    
    // Room management methods
    public void joinRoom(Room room) {
        rooms.add(room);
        this.activeRoomId = room.getId();
    }
    
    public void createRoom(Room room) {
        rooms.add(room);
        this.activeRoomId = room.getId();
    }
    
    public void leaveRoom(Room room) {
        rooms.remove(room);
        if (activeRoomId != null && activeRoomId.equals(room.getId())) {
            activeRoomId = null;
        }
    }
    
    public void setPeer(Peer peer) {
        this.peerId = peer.getId();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public Set<Room> getRooms() {
        return rooms;
    }
    
    public void setRooms(Set<Room> rooms) {
        this.rooms = rooms;
    }
    
    public String getActiveRoomId() {
        return activeRoomId;
    }
    
    public void setActiveRoomId(String activeRoomId) {
        this.activeRoomId = activeRoomId;
    }
    
    public String getPeerId() {
        return peerId;
    }
    
    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }
}