package com.p2pexchange.service;
import com.p2pexchange.model.AccessLevel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.p2pexchange.model.Room;
import com.p2pexchange.repository.RoomRepository;

/**
 * Service for room management using Observer Pattern.
 */
@Service
public class RoomService {
    
    private final RoomRepository roomRepository;
    private List<RoomObserver> observers = new ArrayList<>();
    private Map<String, String> roomTokens = new HashMap<>();
    
    @Autowired
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }
    
    // Observer pattern implementation
    public interface RoomObserver {
        void onRoomEvent(RoomEvent event);
    }
    
    public static class RoomEvent {
        private RoomEventType type;
        private String roomId;
        private String userId;
        private String fileId;
        
        public RoomEvent(RoomEventType type, String roomId, String userId, String fileId) {
            this.type = type;
            this.roomId = roomId;
            this.userId = userId;
            this.fileId = fileId;
        }
        
        public RoomEventType getType() {
            return type;
        }
        
        public String getRoomId() {
            return roomId;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public String getFileId() {
            return fileId;
        }
    }
    
    public enum RoomEventType {
        ROOM_CREATED,
        USER_JOINED,
        USER_LEFT,
        FILE_SHARED,
        ROOM_DELETED
    }
    
    public void addObserver(RoomObserver observer) {
        observers.add(observer);
    }
    
    public void removeObserver(RoomObserver observer) {
        observers.remove(observer);
    }
    
    private void notifyObservers(RoomEvent event) {
        for (RoomObserver observer : observers) {
            observer.onRoomEvent(event);
        }
    }
    
    // Room management methods
    public Room createRoom(String name, String ownerId) {
        Room room = new Room(name, ownerId);
        Room savedRoom = roomRepository.save(room);
        
        // Notify observers
        notifyObservers(new RoomEvent(RoomEventType.ROOM_CREATED, savedRoom.getId(), ownerId, null));
        
        return savedRoom;
    }
    
    public Room getRoomById(String roomId) {
        return roomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + roomId));
    }
    
    public List<Room> getRoomsByOwner(String ownerId) {
        return roomRepository.findByOwnerId(ownerId);
    }
    
    public List<Room> getRoomsByMember(String userId) {
        return roomRepository.findByMembersContaining(userId);
    }
    
    public boolean joinRoom(String roomId, String userId) {
        Optional<Room> optionalRoom = roomRepository.findById(roomId);
        if (!optionalRoom.isPresent()) {
            return false;
        }
        
        Room room = optionalRoom.get();
        
        // Don't add if already a member
        if (room.getMembers().contains(userId)) {
            return true;
        }
        
        // Check access level
        room.setAccessLevel(AccessLevel.PUBLIC);
        if (room.getAccessLevel() == AccessLevel.PRIVATE && !room.getOwnerId().equals(userId)) {
            return false;
        }
        
        // Add user to room
        boolean added = room.addMember(userId);
        if (added) {
            roomRepository.save(room);
            
            // Notify observers
            notifyObservers(new RoomEvent(RoomEventType.USER_JOINED, roomId, userId, null));
        }
        
        return added;
    }
    
    public boolean removeMember(String roomId, String userId) {
        Optional<Room> optionalRoom = roomRepository.findById(roomId);
        if (!optionalRoom.isPresent()) {
            return false;
        }
        
        Room room = optionalRoom.get();
        
        // Can't remove owner
        if (room.getOwnerId().equals(userId)) {
            return false;
        }
        
        // Remove user from room
        boolean removed = room.removeMember(userId);
        if (removed) {
            roomRepository.save(room);
            
            // Notify observers
            notifyObservers(new RoomEvent(RoomEventType.USER_LEFT, roomId, userId, null));
        }
        
        return removed;
    }
    
    public boolean addFileToRoom(String roomId, String fileId) {
        Optional<Room> optionalRoom = roomRepository.findById(roomId);
        if (!optionalRoom.isPresent()) {
            return false;
        }
        
        Room room = optionalRoom.get();
        
        // Add file to room
        room.addSharedFile(fileId);
        roomRepository.save(room);
        
        // Notify observers
        notifyObservers(new RoomEvent(RoomEventType.FILE_SHARED, roomId, null, fileId));
        
        return true;
    }
    
    public boolean removeFileFromRoom(String roomId, String fileId) {
        Optional<Room> optionalRoom = roomRepository.findById(roomId);
        if (!optionalRoom.isPresent()) {
            return false;
        }
        
        Room room = optionalRoom.get();
        
        // Remove file from room
        boolean removed = room.removeSharedFile(fileId);
        if (removed) {
            roomRepository.save(room);
        }
        
        return removed;
    }
    
    public void removeFileFromAllRooms(String fileId) {
        List<Room> rooms = roomRepository.findAll();
        
        for (Room room : rooms) {
            if (room.getSharedFiles().contains(fileId)) {
                room.removeSharedFile(fileId);
                roomRepository.save(room);
            }
        }
    }
    
    public Room updateRoom(Room room) {
        // Ensure room exists
        roomRepository.findById(room.getId())
            .orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + room.getId()));
        
        return roomRepository.save(room);
    }
    
    public boolean deleteRoom(String roomId) {
        Optional<Room> optionalRoom = roomRepository.findById(roomId);
        if (!optionalRoom.isPresent()) {
            return false;
        }
        
        Room room = optionalRoom.get();
        
        // Delete room
        roomRepository.delete(room);
        
        // Notify observers
        notifyObservers(new RoomEvent(RoomEventType.ROOM_DELETED, roomId, room.getOwnerId(), null));
        
        return true;
    }
    
    // Access control methods
    public boolean hasAccess(String roomId, String userId) {
        Optional<Room> optionalRoom = roomRepository.findById(roomId);
        if (!optionalRoom.isPresent()) {
            return false;
        }
        
        Room room = optionalRoom.get();
        return room.hasAccess(userId);
    }
    
    public boolean canAccessFile(String fileId, String userId) {
        List<Room> rooms = roomRepository.findBySharedFilesContaining(fileId);
        
        for (Room room : rooms) {
            if (room.hasAccess(userId)) {
                return true;
            }
        }
        
        return false;
    }
    
    public List<Room> getRoomsContainingFile(String fileId) {
        return roomRepository.findBySharedFilesContaining(fileId);
    }
    
    // Room token methods for sharing
    public void saveRoomToken(String roomId, String token) {
        roomTokens.put(token, roomId);
    }
    
    public String getRoomIdByToken(String token) {
        return roomTokens.get(token);
    }
}