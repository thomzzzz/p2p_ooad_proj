package com.p2pexchange.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.p2pexchange.model.User;

/**
 * Repository for user data access operations.
 */
public interface UserRepository extends MongoRepository<User, String> {
    
    /**
     * Find a user by username.
     * 
     * @param username The username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find a user by email.
     * 
     * @param email The email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if a username exists.
     * 
     * @param username The username to check
     * @return True if the username exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if an email exists.
     * 
     * @param email The email to check
     * @return True if the email exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Find users by role.
     * 
     * @param role The role to search for
     * @return List of users with matching role
     */
    List<User> findByRole(String role);
    
    /**
     * Find users by active status.
     * 
     * @param active The active status to search for
     * @return List of users with matching active status
     */
    List<User> findByActive(boolean active);
    
    /**
     * Find users by username containing a query string.
     * 
     * @param query Username query string (case-insensitive)
     * @return List of users matching the criteria
     */
    @Query("{ 'username': { $regex: ?0, $options: 'i' } }")
    List<User> findByUsernameContaining(String query);
    
    /**
     * Find users who last logged in after a specific date.
     * 
     * @param date The date to filter by
     * @return List of users who logged in after the date
     */
    List<User> findByLastLoginAfter(Date date);
    
    /**
     * Find users who last logged in before a specific date.
     * 
     * @param date The date to filter by
     * @return List of users who logged in before the date
     */
    List<User> findByLastLoginBefore(Date date);
    
    /**
     * Find users created after a specific date.
     * 
     * @param date The date to filter by
     * @return List of users created after the date
     */
    List<User> findByCreatedAtAfter(Date date);
    
    /**
     * Find users with a specific profile attribute.
     * 
     * @param key Profile attribute key
     * @param value Profile attribute value
     * @return List of users with matching profile attribute
     */
    @Query("{ 'profileAttributes.?0': ?1 }")
    List<User> findByProfileAttribute(String key, String value);
    
    /**
     * Count users by role.
     * 
     * @param role The role to count
     * @return Count of users with matching role
     */
    long countByRole(String role);
    
    /**
     * Count active users.
     * 
     * @return Count of active users
     */
    long countByActiveTrue();
}