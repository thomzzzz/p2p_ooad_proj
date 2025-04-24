package com.p2p.service;

import com.p2p.model.Room;
import com.p2p.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class RoomService {

    // Make sure this isn't marked as final if it's giving you initialization issues
    private RoomRepository roomRepository;
    
    // Constructor with @Autowired annotation
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
        
        return roomRepository.save(room);
    }
    
    public Room getRoomById(String roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }
    
    public Room getRoomByLink(String link) {
        return roomRepository.findByRoomLink(link)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }
    
    public List<Room> getRoomsByCreator(String creatorId) {
        return roomRepository.findByCreatorId(creatorId);
    }
    
    public List<Room> getRoomsForUser(String userId) {
        return roomRepository.findByMembersContains(userId);
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