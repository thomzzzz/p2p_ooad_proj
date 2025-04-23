package com.p2pexchange.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.p2pexchange.model.Peer;
import com.p2pexchange.repository.PeerRepository;

/**
 * Service for peer management operations.
 */
@Service
public class PeerService {

    private final PeerRepository peerRepository;
    private final ScheduledExecutorService scheduler;
    
    @Autowired
    public PeerService(PeerRepository peerRepository) {
        this.peerRepository = peerRepository;
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        // Schedule inactive peer detection
        this.scheduler.scheduleAtFixedRate(this::markInactivePeers, 1, 5, TimeUnit.MINUTES);
    }
    
    /**
     * Register a new peer.
     * 
     * @param userId The user ID
     * @param ipAddress The IP address
     * @param port The port number
     * @return The registered peer
     */
    public Peer registerPeer(String userId, String ipAddress, int port) {
        // Check if peer already exists
        Optional<Peer> existingPeer = peerRepository.findByUserId(userId);
        
        if (existingPeer.isPresent()) {
            Peer peer = existingPeer.get();
            peer.setIpAddress(ipAddress);
            peer.setPort(port);
            peer.markOnline();
            return peerRepository.save(peer);
        } else {
            Peer newPeer = new Peer(userId, ipAddress, port);
            return peerRepository.save(newPeer);
        }
    }
    
    /**
     * Get a peer by user ID.
     * 
     * @param userId The user ID
     * @return The peer, or null if not found
     */
    public Peer getPeerByUserId(String userId) {
        return peerRepository.findByUserId(userId).orElse(null);
    }
    
    /**
     * Get all active peers.
     * 
     * @return List of active peers
     */
    public List<Peer> getActivePeers() {
        return peerRepository.findByOnline(true);
    }
    
    /**
     * Get active peers seen after a specific date.
     * 
     * @param date The cutoff date
     * @return List of active peers seen after the date
     */
    public List<Peer> getActivePeersSeenAfter(Date date) {
        return peerRepository.findByOnlineTrueAndLastSeenAfter(date);
    }
    
    /**
     * Get recently active peers (seen in the last hour).
     * 
     * @return List of recently active peers
     */
    public List<Peer> getRecentlyActivePeers() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1);
        return getActivePeersSeenAfter(cal.getTime());
    }
    
    /**
     * Mark a peer as online.
     * 
     * @param userId The user ID
     * @return The updated peer, or null if not found
     */
    public Peer markPeerOnline(String userId) {
        Optional<Peer> optionalPeer = peerRepository.findByUserId(userId);
        
        if (optionalPeer.isPresent()) {
            Peer peer = optionalPeer.get();
            peer.markOnline();
            return peerRepository.save(peer);
        }
        
        return null;
    }
    
    /**
     * Mark a peer as offline.
     * 
     * @param userId The user ID
     * @return The updated peer, or null if not found
     */
    public Peer markPeerOffline(String userId) {
        Optional<Peer> optionalPeer = peerRepository.findByUserId(userId);
        
        if (optionalPeer.isPresent()) {
            Peer peer = optionalPeer.get();
            peer.markOffline();
            return peerRepository.save(peer);
        }
        
        return null;
    }
    
    /**
     * Update a peer's IP address and port.
     * 
     * @param userId The user ID
     * @param ipAddress The new IP address
     * @param port The new port number
     * @return The updated peer, or null if not found
     */
    public Peer updatePeerAddress(String userId, String ipAddress, int port) {
        Optional<Peer> optionalPeer = peerRepository.findByUserId(userId);
        
        if (optionalPeer.isPresent()) {
            Peer peer = optionalPeer.get();
            peer.setIpAddress(ipAddress);
            peer.setPort(port);
            peer.markOnline();
            return peerRepository.save(peer);
        }
        
        return null;
    }
    
    /**
     * Get peers by IP address.
     * 
     * @param ipAddress The IP address
     * @return List of peers with the IP address
     */
    public List<Peer> getPeersByIpAddress(String ipAddress) {
        return peerRepository.findByIpAddress(ipAddress);
    }
    
    /**
     * Get peers by IP address pattern.
     * 
     * @param ipPattern The IP address pattern
     * @return List of peers matching the pattern
     */
    public List<Peer> getPeersByIpPattern(String ipPattern) {
        return peerRepository.findByIpAddressMatching(ipPattern);
    }
    
    /**
     * Delete a peer.
     * 
     * @param userId The user ID
     */
    public void deletePeer(String userId) {
        peerRepository.deleteByUserId(userId);
    }
    
    /**
     * Count online peers.
     * 
     * @return The count of online peers
     */
    public long countOnlinePeers() {
        return peerRepository.countByOnlineTrue();
    }
    
    /**
     * Get a peer's connection string.
     * 
     * @param userId The user ID
     * @return The connection string (IP:port), or null if not found
     */
    public String getPeerConnectionString(String userId) {
        Peer peer = getPeerByUserId(userId);
        
        if (peer != null && peer.isOnline()) {
            return peer.getConnectionString();
        }
        
        return null;
    }
    
    /**
     * Check if a peer is active.
     * 
     * @param userId The user ID
     * @return True if the peer is active, false otherwise
     */
    public boolean isPeerActive(String userId) {
        Peer peer = getPeerByUserId(userId);
        return peer != null && peer.isActive();
    }
    
    /**
     * Mark inactive peers as offline.
     * This method is called periodically by the scheduler.
     */
    private void markInactivePeers() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -5);  // Peers not seen for 5 minutes are considered inactive
        
        List<Peer> inactivePeers = peerRepository.findInactivePeers(cal.getTime());
        
        for (Peer peer : inactivePeers) {
            peer.markOffline();
            peerRepository.save(peer);
        }
    }
    
    /**
     * Shutdown the service.
     * This should be called when the application is shutting down.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}