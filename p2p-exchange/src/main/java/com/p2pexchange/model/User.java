package com.p2pexchange.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * User model representing an application user.
 */
@Document(collection = "users")
public class User {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String username;
    
    private String passwordHash;
    
    @Indexed(unique = true)
    private String email;
    
    private String role;
    private Date createdAt;
    private Date lastLogin;
    private boolean active;
    private Map<String, String> profileAttributes = new HashMap<>();
    
    // Default constructor
    public User() {
        this.createdAt = new Date();
        this.active = true;
        this.role = "ROLE_USER"; // Default role
    }
    
    // Constructor with fields
    public User(String username, String passwordHash, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.createdAt = new Date();
        this.active = true;
        this.role = "ROLE_USER"; // Default role
    }
    
    // Profile management methods
    public void setProfileAttribute(String key, String value) {
        profileAttributes.put(key, value);
    }
    
    public String getProfileAttribute(String key) {
        return profileAttributes.get(key);
    }
    
    public void removeProfileAttribute(String key) {
        profileAttributes.remove(key);
    }
    
    // Login/activity tracking
    public void recordLogin() {
        this.lastLogin = new Date();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public Map<String, String> getProfileAttributes() {
        return profileAttributes;
    }
    
    public void setProfileAttributes(Map<String, String> profileAttributes) {
        this.profileAttributes = profileAttributes;
    }
    
    // Admin check
    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(role);
    }
}