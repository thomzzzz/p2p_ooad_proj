package com.p2p.repository;

import com.p2p.model.Peer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PeerRepository extends MongoRepository<Peer, String> {
    Optional<Peer> findByUserId(String userId);
    List<Peer> findByIsOnline(boolean isOnline);
}