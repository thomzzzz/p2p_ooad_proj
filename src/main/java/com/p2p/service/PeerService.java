package com.p2p.service;

import com.p2p.model.Peer;
import com.p2p.repository.PeerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PeerService {

    // Removed 'final' modifier to avoid initialization issues
    private PeerRepository peerRepository;
    
    @Autowired
    public PeerService(PeerRepository peerRepository) {
        this.peerRepository = peerRepository;
    }
    
    public Peer registerPeer(String userId, String ipAddress, int port) {
        Peer peer = new Peer();
        peer.setUserId(userId);
        peer.setIpAddress(ipAddress);
        peer.setPort(port);
        peer.setOnline(true);
        
        return peerRepository.save(peer);
    }
    
    public Peer getPeerById(String peerId) {
        return peerRepository.findById(peerId)
                .orElseThrow(() -> new RuntimeException("Peer not found"));
    }
    
    public Peer getPeerByUserId(String userId) {
        return peerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Peer not found for user"));
    }
    
    public List<Peer> getOnlinePeers() {
        return peerRepository.findByIsOnline(true);
    }
    
    public Peer setOnlineStatus(String peerId, boolean isOnline) {
        Peer peer = getPeerById(peerId);
        peer.setOnline(isOnline);
        return peerRepository.save(peer);
    }
    
    public void deletePeer(String peerId) {
        peerRepository.deleteById(peerId);
    }
}