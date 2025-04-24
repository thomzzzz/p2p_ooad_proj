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

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    // Removed 'final' modifier
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