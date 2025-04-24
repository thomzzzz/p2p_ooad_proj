package com.p2p.repository;

import com.p2p.model.Room;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends MongoRepository<Room, String> {
    List<Room> findByCreatorId(String creatorId);
    
    // Use a more explicit query to ensure it works correctly
    @Query("{ 'members' : ?0 }")
    List<Room> findByMembersContains(String userId);
    
    Optional<Room> findByRoomLink(String roomLink);
}