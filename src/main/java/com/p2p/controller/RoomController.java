package com.p2p.controller;

import com.p2p.model.Room;
import com.p2p.model.User;
import com.p2p.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private RoomService roomService;
    
    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }
    
    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Map<String, String> request, 
                                           @AuthenticationPrincipal User user) {
        String name = request.get("name");
        Room room = roomService.createRoom(name, user.getId());
        return ResponseEntity.ok(room);
    }
    
    @GetMapping
    public ResponseEntity<List<Room>> getMyRooms(@AuthenticationPrincipal User user) {
        List<Room> rooms = roomService.getRoomsForUser(user.getId());
        return ResponseEntity.ok(rooms);
    }
    
    @GetMapping("/{roomId}")
    public ResponseEntity<Room> getRoom(@PathVariable String roomId) {
        Room room = roomService.getRoomById(roomId);
        return ResponseEntity.ok(room);
    }
    
    @GetMapping("/link/{link}")
    public ResponseEntity<Room> getRoomByLink(@PathVariable String link) {
        Room room = roomService.getRoomByLink(link);
        return ResponseEntity.ok(room);
    }
    
    // Debug endpoint
    @GetMapping("/debug/members/{roomId}")
    public ResponseEntity<?> debugRoomMembers(@PathVariable String roomId) {
        try {
            Room room = roomService.getRoomById(roomId);
            Map<String, Object> response = new HashMap<>();
            response.put("roomId", room.getId());
            response.put("roomName", room.getName());
            response.put("roomLink", room.getRoomLink());
            response.put("creatorId", room.getCreatorId());
            response.put("memberCount", room.getMembers().size());
            response.put("members", room.getMembers());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/debug/user-rooms/{userId}")
    public ResponseEntity<?> debugUserRooms(@PathVariable String userId) {
        try {
            List<Room> rooms = roomService.getRoomsForUser(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("roomCount", rooms.size());
            
            List<Map<String, Object>> roomDetails = new ArrayList<>();
            for (Room room : rooms) {
                Map<String, Object> details = new HashMap<>();
                details.put("roomId", room.getId());
                details.put("roomName", room.getName());
                details.put("memberCount", room.getMembers().size());
                details.put("isCreator", room.getCreatorId().equals(userId));
                roomDetails.add(details);
            }
            
            response.put("rooms", roomDetails);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    // NEW ENDPOINT: Join room by link
    @PostMapping("/join/{link}")
    public ResponseEntity<Room> joinRoomByLink(@PathVariable String link, 
                                              @AuthenticationPrincipal User user) {
        Room room = roomService.joinRoomByLink(link, user.getId());
        return ResponseEntity.ok(room);
    }
    
    @PostMapping("/{roomId}/members")
    public ResponseEntity<Room> addMember(@PathVariable String roomId, 
                                          @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        Room room = roomService.addMemberToRoom(roomId, userId);
        return ResponseEntity.ok(room);
    }
    
    @DeleteMapping("/{roomId}/members/{userId}")
    public ResponseEntity<Room> removeMember(@PathVariable String roomId, 
                                            @PathVariable String userId) {
        Room room = roomService.removeMemberFromRoom(roomId, userId);
        return ResponseEntity.ok(room);
    }
    
    @PostMapping("/{roomId}/files")
    public ResponseEntity<Room> addFile(@PathVariable String roomId, 
                                        @RequestBody Map<String, String> request) {
        String fileId = request.get("fileId");
        Room room = roomService.addFileToRoom(roomId, fileId);
        return ResponseEntity.ok(room);
    }
    
    @DeleteMapping("/{roomId}/files/{fileId}")
    public ResponseEntity<Room> removeFile(@PathVariable String roomId, 
                                          @PathVariable String fileId) {
        Room room = roomService.removeFileFromRoom(roomId, fileId);
        return ResponseEntity.ok(room);
    }
    
    @PostMapping("/{roomId}/generate-link")
    public ResponseEntity<Map<String, String>> generateLink(@PathVariable String roomId) {
        String link = roomService.generateRoomLink(roomId);
        return ResponseEntity.ok(Map.of("link", link));
    }
    
    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable String roomId, 
                                       @AuthenticationPrincipal User user) {
        // Verify user is room creator
        Room room = roomService.getRoomById(roomId);
        if (!room.getCreatorId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Not authorized to delete this room");
        }
        
        roomService.deleteRoom(roomId);
        return ResponseEntity.ok().build();
    }
}