package com.p2p.controller;

import com.p2p.model.Peer;
import com.p2p.model.User;
import com.p2p.service.SimplePeerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/peers")
public class PeerController {

    private SimplePeerService peerService;
    
    @Autowired
    public PeerController(SimplePeerService peerService) {
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
                                             @RequestBody Map<String, Boolean> request,
                                             @AuthenticationPrincipal User user) {
        boolean isOnline = request.get("online");
        peerService.setPeerStatus(user.getId(), isOnline);
        
        // Since we don't have the exact method in SimplePeerService, we'll just return success
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{peerId}")
    public ResponseEntity<?> deletePeer(@PathVariable String peerId,
                                       @AuthenticationPrincipal User user) {
        // Set the peer as offline instead of deleting
        peerService.setPeerStatus(user.getId(), false);
        return ResponseEntity.ok().build();
    }
}