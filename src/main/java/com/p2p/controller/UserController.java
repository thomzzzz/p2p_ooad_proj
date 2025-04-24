package com.p2p.controller;

import com.p2p.model.User;
import com.p2p.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    // Removed 'final' modifier
    private UserService userService;
    
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/current")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        
        // Don't expose the password
        user.setPassword(null);
        
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/current")
    public ResponseEntity<User> updateCurrentUser(@RequestBody User updatedUser, 
                                                 @AuthenticationPrincipal User currentUser) {
        // Only update allowed fields
        currentUser.setEmail(updatedUser.getEmail());
        
        // Don't allow role change through this endpoint
        
        User savedUser = userService.updateUser(currentUser);
        
        // Don't expose the password
        savedUser.setPassword(null);
        
        return ResponseEntity.ok(savedUser);
    }
}