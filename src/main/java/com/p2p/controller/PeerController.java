package com.p2p.controller;

import com.p2p.model.Peer;
import com.p2p.model.User;
import com.p2p.service.PeerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/peers")
public class PeerController {

    // Removed 'final' modifier
    private PeerService peerService;
    
    @Autowired
    public PeerController(PeerService peerService) {
        this.peerService = peerService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<Peer> registerPeer(@RequestBody Map<String, Object> request,
                                             @AuthenticationPrincipal User user) {
        String ipAddress = (String) request.get("ipAddress");
        int port = (int) request.get("port");
        
        Peer peer = peerService.registerPeer(user.getId(), ipAddress, port);
        return ResponseEntity.ok(peer);
    }
    
    @GetMapping("/online")
    public ResponseEntity<List<Peer>> getOnlinePeers() {
        List<Peer> peers = peerService.getOnlinePeers();
        return ResponseEntity.ok(peers);
    }
    
    @PutMapping("/{peerId}/status")
    public ResponseEntity<Peer> updateStatus(@PathVariable String peerId,
                                             @RequestBody Map<String, Boolean> request) {
        boolean isOnline = request.get("online");
        Peer peer = peerService.setOnlineStatus(peerId, isOnline);
        return ResponseEntity.ok(peer);
    }
    
    @DeleteMapping("/{peerId}")
    public ResponseEntity<?> deletePeer(@PathVariable String peerId,
                                       @AuthenticationPrincipal User user) {
        // Check if user owns the peer
        Peer peer = peerService.getPeerById(peerId);
        if (!peer.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Not authorized to delete this peer");
        }
        
        peerService.deletePeer(peerId);
        return ResponseEntity.ok().build();
    }
}