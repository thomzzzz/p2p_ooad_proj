package com.p2p.service;

import com.p2p.model.Peer;
import com.p2p.repository.PeerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Service
public class PeerRegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(PeerRegistrationService.class);
    private static final long ONLINE_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes
    
    private PeerRepository peerRepository;
    
    @Autowired
    public PeerRegistrationService(PeerRepository peerRepository) {
        this.peerRepository = peerRepository;
    }
    
    public Peer registerPeer(String userId, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        int port = request.getRemotePort();
        
        logger.info("Registering peer - userId: {}, IP: {}, port: {}", userId, ipAddress, port);
        
        // Check if peer already exists
        Peer existingPeer = peerRepository.findByUserId(userId).orElse(null);
        
        if (existingPeer != null) {
            // Update existing peer
            existingPeer.setIpAddress(ipAddress);
            existingPeer.setPort(port);
            existingPeer.setOnline(true);
            existingPeer.setLastSeen(new Date());
            
            logger.info("Updated existing peer: {}", existingPeer.getId());
            return peerRepository.save(existingPeer);
        } else {
            // Create new peer
            Peer newPeer = new Peer();
            newPeer.setUserId(userId);
            newPeer.setIpAddress(ipAddress);
            newPeer.setPort(port);
            newPeer.setOnline(true);
            newPeer.setLastSeen(new Date());
            
            logger.info("Created new peer");
            return peerRepository.save(newPeer);
        }
    }
    
    public void updatePeerStatus(String userId, boolean isOnline) {
        Peer peer = peerRepository.findByUserId(userId).orElse(null);
        
        if (peer != null) {
            peer.setOnline(isOnline);
            peer.setLastSeen(new Date());
            peerRepository.save(peer);
            logger.info("Updated peer status - userId: {}, online: {}", userId, isOnline);
        } else {
            logger.warn("Attempted to update status for non-existent peer - userId: {}", userId);
        }
    }
    
    public List<Peer> getOnlinePeers() {
        List<Peer> onlinePeers = peerRepository.findByIsOnline(true);
        logger.info("Retrieved {} online peers", onlinePeers.size());
        return onlinePeers;
    }
    
    @Scheduled(fixedRate = 60000) // Run every minute
    public void checkPeerTimeouts() {
        logger.debug("Checking peer timeouts...");
        List<Peer> onlinePeers = peerRepository.findByIsOnline(true);
        Date now = new Date();
        
        int timeoutCount = 0;
        for (Peer peer : onlinePeers) {
            if (peer.getLastSeen() != null) {
                long timeSinceLastSeen = now.getTime() - peer.getLastSeen().getTime();
                
                if (timeSinceLastSeen > ONLINE_TIMEOUT_MS) {
                    peer.setOnline(false);
                    peerRepository.save(peer);
                    timeoutCount++;
                    logger.debug("Peer timed out - userId: {}, lastSeen: {}", peer.getUserId(), peer.getLastSeen());
                }
            }
        }
        
        if (timeoutCount > 0) {
            logger.info("Marked {} peers as offline due to timeout", timeoutCount);
        }
    }
    
    // Get client IP address, handling proxies correctly
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Get the first IP which is the client's IP
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}