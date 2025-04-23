package com.p2pexchange.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.p2pexchange.model.User;
import com.p2pexchange.repository.UserRepository;

/**
 * Service for user management using Singleton Pattern.
 */
@Service
public class UserService implements UserDetailsService {
    
    private static UserService instance;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    // Singleton pattern implementation
    public static synchronized UserService getInstance(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        if (instance == null) {
            instance = new UserService(userRepository, passwordEncoder);
        }
        return instance;
    }
    
    // UserDetailsService implementation
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));
        
        return new org.springframework.security.core.userdetails.User(
                user.getId(), 
                user.getPasswordHash(),
                user.isActive(),
                true, // account non-expired
                true, // credentials non-expired
                true, // account non-locked
                authorities);
    }
    
    // User management methods
    public User createUser(User user) {
        // Encode password
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        
        // Set default role if not specified
        if (user.getRole() == null) {
            user.setRole("ROLE_USER");
        }
        
        return userRepository.save(user);
    }
    
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
    }
    
    public List<User> getUsersByIds(Iterable<String> ids) {
        Iterable<User> userIterable = userRepository.findAllById(ids);
        List<User> users = new ArrayList<>();
        userIterable.forEach(users::add);
        return users;
    }
    
    public User updateUser(User user) {
        // Ensure user exists
        userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + user.getId()));
        
        return userRepository.save(user);
    }
    
    public void changePassword(String username, String newPassword) {
        User user = getUserByUsername(username);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    // Authentication methods
    public User authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (!userOpt.isPresent()) {
            return null;
        }
        
        User user = userOpt.get();
        
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return null;
        }
        
        // Record login time
        user.recordLogin();
        userRepository.save(user);
        
        return user;
    }
    
    // Admin methods
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
    
    public void activateUser(String id) {
        User user = getUserById(id);
        user.setActive(true);
        userRepository.save(user);
    }
    
    public void deactivateUser(String id) {
        User user = getUserById(id);
        user.setActive(false);
        userRepository.save(user);
    }
}