package com.p2pexchange.security;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Implementation of Role-Based Access Control (RBAC).
 */
@Component
public class RoleBasedAccessControl {

    // Map of roles to permissions
    private final Map<String, Set<String>> rolePermissions = new HashMap<>();
    
    // Constructor initializes the permissions for different roles
    public RoleBasedAccessControl() {
        // Initialize permissions for admin role
        Set<String> adminPermissions = new HashSet<>();
        adminPermissions.add("user:create");
        adminPermissions.add("user:read");
        adminPermissions.add("user:update");
        adminPermissions.add("user:delete");
        adminPermissions.add("file:create");
        adminPermissions.add("file:read");
        adminPermissions.add("file:update");
        adminPermissions.add("file:delete");
        adminPermissions.add("room:create");
        adminPermissions.add("room:read");
        adminPermissions.add("room:update");
        adminPermissions.add("room:delete");
        adminPermissions.add("system:access");
        rolePermissions.put("ROLE_ADMIN", adminPermissions);
        
        // Initialize permissions for standard user role
        Set<String> userPermissions = new HashSet<>();
        userPermissions.add("user:read");
        userPermissions.add("user:update");  // Can update own user
        userPermissions.add("file:create");
        userPermissions.add("file:read");
        userPermissions.add("file:update");  // Can update own files
        userPermissions.add("file:delete");  // Can delete own files
        userPermissions.add("room:create");
        userPermissions.add("room:read");
        userPermissions.add("room:update");  // Can update own rooms
        userPermissions.add("room:delete");  // Can delete own rooms
        rolePermissions.put("ROLE_USER", userPermissions);
        
        // Initialize permissions for guest role (if applicable)
        Set<String> guestPermissions = new HashSet<>();
        guestPermissions.add("user:read");
        guestPermissions.add("file:read");
        guestPermissions.add("room:read");
        rolePermissions.put("ROLE_GUEST", guestPermissions);
    }
    
    /**
     * Check if a role has a specific permission.
     * 
     * @param role The role to check
     * @param permission The permission to check for
     * @return True if the role has the permission, false otherwise
     */
    public boolean hasPermission(String role, String permission) {
        Set<String> permissions = rolePermissions.get(role);
        return permissions != null && permissions.contains(permission);
    }
    
    /**
     * Check if a user with certain roles has a specific permission.
     * 
     * @param roles The roles to check
     * @param permission The permission to check for
     * @return True if any of the roles has the permission, false otherwise
     */
    public boolean hasPermission(Set<String> roles, String permission) {
        for (String role : roles) {
            if (hasPermission(role, permission)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get all permissions for a role.
     * 
     * @param role The role to get permissions for
     * @return Set of permissions for the role
     */
    public Set<String> getPermissions(String role) {
        return rolePermissions.getOrDefault(role, new HashSet<>());
    }
    
    /**
     * Add a permission to a role.
     * 
     * @param role The role to add the permission to
     * @param permission The permission to add
     */
    public void addPermission(String role, String permission) {
        rolePermissions.computeIfAbsent(role, k -> new HashSet<>()).add(permission);
    }
    
    /**
     * Remove a permission from a role.
     * 
     * @param role The role to remove the permission from
     * @param permission The permission to remove
     */
    public void removePermission(String role, String permission) {
        Set<String> permissions = rolePermissions.get(role);
        if (permissions != null) {
            permissions.remove(permission);
        }
    }
    
    /**
     * Check if a user is authorized to access an owned resource.
     * 
     * @param role The user's role
     * @param permission The permission to check for
     * @param resourceOwnerId The resource owner's ID
     * @param userId The user's ID
     * @return True if the user is authorized, false otherwise
     */
    public boolean isAuthorized(String role, String permission, String resourceOwnerId, String userId) {
        // Admins have full access
        if ("ROLE_ADMIN".equals(role)) {
            return true;
        }
        
        // Check if user has the permission
        boolean hasPermission = hasPermission(role, permission);
        
        // If own resource, user with permission can access
        if (resourceOwnerId.equals(userId) && hasPermission) {
            return true;
        }
        
        // For read operations, check specific permissions
        if (permission.endsWith(":read") && hasPermission) {
            return true;
        }
        
        return false;
    }
}