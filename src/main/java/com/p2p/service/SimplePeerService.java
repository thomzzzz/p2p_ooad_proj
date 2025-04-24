package com.p2p.service;

import com.p2p.model.Peer;
import com.p2p.repository.PeerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * A simplified peer service without scheduling or complex dependencies
 */
@Service
public class SimplePeerService {

    private static final Logger logger = LoggerFactory.getLogger(SimplePeerService.class);
    
    private final PeerRepository peerRepository;
    
    @Autowired
    public SimplePeerService(PeerRepository peerRepository) {
        this.peerRepository = peerRepository;
    }
    
    /**
     * Register a peer as online
     */
    public Peer registerPeer(String userId, String ipAddress, int port) {
        try {
            logger.debug("Registering peer: userId={}, ip={}, port={}", userId, ipAddress, port);
            
            // Check if peer already exists
            Peer existingPeer = peerRepository.findByUserId(userId).orElse(null);
            
            if (existingPeer != null) {
                existingPeer.setIpAddress(ipAddress);
                existingPeer.setPort(port);
                existingPeer.setOnline(true);
                return peerRepository.save(existingPeer);
            } else {
                // Create new peer
                Peer newPeer = new Peer();
                newPeer.setUserId(userId);
                newPeer.setIpAddress(ipAddress);
                newPeer.setPort(port);
                newPeer.setOnline(true);
                return peerRepository.save(newPeer);
            }
        } catch (Exception e) {
            logger.error("Error registering peer", e);
            return null;
        }
    }
    
    /**
     * Get list of online peers
     */
    public List<Peer> getOnlinePeers() {
        try {
            return peerRepository.findByIsOnline(true);
        } catch (Exception e) {
            logger.error("Error getting online peers", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Set peer online status
     */
    public void setPeerStatus(String userId, boolean isOnline) {
        try {
            Peer peer = peerRepository.findByUserId(userId).orElse(null);
            if (peer != null) {
                peer.setOnline(isOnline);
                peerRepository.save(peer);
            }
        } catch (Exception e) {
            logger.error("Error setting peer status", e);
        }
    }
}
