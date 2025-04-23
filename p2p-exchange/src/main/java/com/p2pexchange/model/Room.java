package com.p2pexchange.model;
import com.p2pexchange.model.AccessLevel;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;

@Document(collection = "rooms")
public class Room {
    @Id
    private String id;
    private String name;
    private Set<String> members = new HashSet<>();
    private Set<String> creators = new HashSet<>();
    private List<String> sharedFiles = new ArrayList<>();
    private AccessLevel accessLevel = AccessLevel.PUBLIC;
    private String ownerId;
    
    // Default constructor
    public Room() {
    }
    
    // Constructor with fields
    public Room(String name, String ownerId) {
        this.name = name;
        this.ownerId = ownerId;
        this.members.add(ownerId);  // Owner is automatically a member
        this.creators.add(ownerId); // Owner is automatically a creator
    }
    
    // Methods
    public boolean addMember(String userId) {
        return members.add(userId);
    }
    
    public boolean removeMember(String userId) {
        if (userId.equals(ownerId)) {
            return false;  // Cannot remove the owner
        }
        boolean removed = members.remove(userId);
        creators.remove(userId);  // Also remove from creators if present
        return removed;
    }
    
    public boolean addCreator(String userId) {
        if (!members.contains(userId)) {
            return false;  // Must be a member to be a creator
        }
        return creators.add(userId);
    }
    
    public boolean removeCreator(String userId) {
        if (userId.equals(ownerId)) {
            return false;  // Cannot remove owner from creators
        }
        return creators.remove(userId);
    }
    
    public void addSharedFile(String fileId) {
        sharedFiles.add(fileId);
    }
    
    public boolean removeSharedFile(String fileId) {
        return sharedFiles.remove(fileId);
    }
    
    public boolean hasAccess(String userId) {
        return members.contains(userId);
    }
    
    public boolean canShareFiles(String userId) {
        return creators.contains(userId);
    }
    
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
    
    public Set<String> getMembers() {
        return members;
    }
    
    public void setMembers(Set<String> members) {
        this.members = members;
    }
    
    public Set<String> getCreators() {
        return creators;
    }
    
    public void setCreators(Set<String> creators) {
        this.creators = creators;
    }
    
    public List<String> getSharedFiles() {
        return sharedFiles;
    }
    
    public void setSharedFiles(List<String> sharedFiles) {
        this.sharedFiles = sharedFiles;
    }
    
    public AccessLevel getAccessLevel() {
        return accessLevel;
    }
    
    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }
    
    public String getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}

// Enum for access levels
enum AccessLevel {
    PUBLIC,     // Anyone with link can join
    RESTRICTED, // Requires approval from room owner
    PRIVATE     // Invite only
}