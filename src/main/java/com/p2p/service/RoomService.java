package com.p2p.service;

import com.p2p.model.Room;
import com.p2p.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);
    
    private RoomRepository roomRepository;
    
    @Autowired
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }
    
    public Room createRoom(String name, String creatorId) {
        Room room = new Room();
        room.setName(name);
        room.setCreatorId(creatorId);
        room.getMembers().add(creatorId); // Add creator as a member
        room.setCreatedAt(new Date());
        room.setRoomLink(UUID.randomUUID().toString());
        
        logger.debug("Creating room: {} with creator: {}", name, creatorId);
        return roomRepository.save(room);
    }
    
    public Room getRoomById(String roomId) {
        logger.debug("Fetching room by ID: {}", roomId);
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }
    
    public Room getRoomByLink(String link) {
        logger.debug("Fetching room by link: {}", link);
        return roomRepository.findByRoomLink(link)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }
    
    public List<Room> getRoomsByCreator(String creatorId) {
        logger.debug("Fetching rooms created by: {}", creatorId);
        return roomRepository.findByCreatorId(creatorId);
    }
    
    public List<Room> getRoomsForUser(String userId) {
        logger.debug("Fetching rooms where user {} is a member", userId);
        List<Room> rooms = roomRepository.findByMembersContains(userId);
        logger.debug("Found {} rooms for user {}", rooms.size(), userId);
        return rooms;
    }
    
    // NEW METHOD: Join room by link
    public Room joinRoomByLink(String link, String userId) {
        logger.debug("User {} is joining room with link: {}", userId, link);
        Room room = roomRepository.findByRoomLink(link)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        
        // Check if the user is already a member
        if (!room.getMembers().contains(userId)) {
            logger.debug("Adding user {} to room {}", userId, room.getId());
            room.getMembers().add(userId);
            room = roomRepository.save(room);
        } else {
            logger.debug("User {} is already a member of room {}", userId, room.getId());
        }
        
        return room;
    }
    
    public Room addMemberToRoom(String roomId, String userId) {
        Room room = getRoomById(roomId);
        room.addMember(userId);
        return roomRepository.save(room);
    }
    
    public Room removeMemberFromRoom(String roomId, String userId) {
        Room room = getRoomById(roomId);
        room.removeMember(userId);
        return roomRepository.save(room);
    }
    
    public Room addFileToRoom(String roomId, String fileId) {
        Room room = getRoomById(roomId);
        room.addFile(fileId);
        return roomRepository.save(room);
    }
    
    public Room removeFileFromRoom(String roomId, String fileId) {
        Room room = getRoomById(roomId);
        room.removeFile(fileId);
        return roomRepository.save(room);
    }
    
    public String generateRoomLink(String roomId) {
        Room room = getRoomById(roomId);
        String link = UUID.randomUUID().toString();
        room.setRoomLink(link);
        roomRepository.save(room);
        return link;
    }
    
    public void deleteRoom(String roomId) {
        roomRepository.deleteById(roomId);
    }
}