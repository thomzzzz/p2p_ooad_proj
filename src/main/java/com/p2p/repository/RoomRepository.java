package com.p2p.repository;

import com.p2p.model.Room;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends MongoRepository<Room, String> {
    List<Room> findByCreatorId(String creatorId);
    List<Room> findByMembersContains(String userId);
    Optional<Room> findByRoomLink(String roomLink);
}