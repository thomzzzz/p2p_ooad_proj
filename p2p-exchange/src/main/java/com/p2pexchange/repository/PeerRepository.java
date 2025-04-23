package com.p2pexchange.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.p2pexchange.model.Peer;

/**
 * Repository for peer data access operations.
 */
public interface PeerRepository extends MongoRepository<Peer, String> {
    
    /**
     * Find a peer by associated user ID.
     * 
     * @param userId The user's ID
     * @return Optional containing the peer if found
     */
    Optional<Peer> findByUserId(String userId);
    
    /**
     * Find peers by online status.
     * 
     * @param online The online status to search for
     * @return List of peers with matching online status
     */
    List<Peer> findByOnline(boolean online);
    
    /**
     * Find online peers seen after a specific date.
     * 
     * @param date The date to filter by
     * @return List of online peers seen after the date
     */
    List<Peer> findByOnlineTrueAndLastSeenAfter(Date date);
    
    /**
     * Find peers by IP address.
     * 
     * @param ipAddress The IP address to search for
     * @return List of peers with matching IP address
     */
    List<Peer> findByIpAddress(String ipAddress);
    
    /**
     * Find peers by IP address pattern.
     * 
     * @param ipPattern The IP address pattern to search for
     * @return List of peers with matching IP address pattern
     */
    @Query("{ 'ipAddress': { $regex: ?0 } }")
    List<Peer> findByIpAddressMatching(String ipPattern);
    
    /**
     * Find peers with a specific port number.
     * 
     * @param port The port number to search for
     * @return List of peers with matching port number
     */
    List<Peer> findByPort(int port);
    
    /**
     * Delete peers by user ID.
     * 
     * @param userId The user's ID
     */
    void deleteByUserId(String userId);
    
    /**
     * Find inactive peers (not seen for a specified period).
     * 
     * @param cutoffDate The date threshold for inactivity
     * @return List of inactive peers
     */
    @Query("{ 'lastSeen': { $lt: ?0 }, 'online': true }")
    List<Peer> findInactivePeers(Date cutoffDate);
    
    /**
     * Count online peers.
     * 
     * @return Count of online peers
     */
    long countByOnlineTrue();
}