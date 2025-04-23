package com.p2pexchange.controller;
import com.p2pexchange.model.AccessLevel;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.p2pexchange.model.Room;
import com.p2pexchange.model.User;
import com.p2pexchange.model.File;
import com.p2pexchange.service.RoomService;
import com.p2pexchange.service.UserService;
import com.p2pexchange.service.FileService;

@Controller
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;
    private final UserService userService;
    private final FileService fileService;

    @Autowired
    public RoomController(RoomService roomService, UserService userService, FileService fileService) {
        this.roomService = roomService;
        this.userService = userService;
        this.fileService = fileService;
    }

    @GetMapping
    public String listRooms(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        List<Room> ownedRooms = roomService.getRoomsByOwner(userId);
        List<Room> memberRooms = roomService.getRoomsByMember(userId);
        
        model.addAttribute("ownedRooms", ownedRooms);
        model.addAttribute("memberRooms", memberRooms);
        
        return "rooms/list";
    }

    @GetMapping("/{roomId}")
    public String viewRoom(@PathVariable String roomId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        Room room = roomService.getRoomById(roomId);
        
        // Check if user has access to this room
        if (!roomService.hasAccess(roomId, userId)) {
            return "redirect:/rooms?error=access";
        }
        
        List<User> roomMembers = userService.getUsersByIds(room.getMembers());
        List<File> roomFiles = fileService.getFilesByIds(room.getSharedFiles());
        
        model.addAttribute("room", room);
        model.addAttribute("roomMembers", roomMembers);
        model.addAttribute("roomFiles", roomFiles);
        model.addAttribute("isOwner", userId.equals(room.getOwnerId()));
        
        return "rooms/view";
    }

    @GetMapping("/create")
    public String showCreateForm() {
        return "rooms/create";
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<Room> createRoom(@RequestBody Map<String, String> roomData) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        String roomName = roomData.get("name");
        if (roomName == null || roomName.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Room room = roomService.createRoom(roomName, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }

    @PostMapping("/{roomId}/join")
    @ResponseBody
    public ResponseEntity<Void> joinRoom(@PathVariable String roomId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        boolean joined = roomService.joinRoom(roomId, userId);
        
        if (joined) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/join/{token}")
    public String joinRoomByToken(@PathVariable String token) {
        String roomId = roomService.getRoomIdByToken(token);
        
        if (roomId == null) {
            return "redirect:/rooms?error=invalid_token";
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        boolean joined = roomService.joinRoom(roomId, userId);
        
        if (joined) {
            return "redirect:/rooms/" + roomId;
        } else {
            return "redirect:/rooms?error=join_failed";
        }
    }

    @PostMapping("/{roomId}/link")
    @ResponseBody
    public ResponseEntity<Map<String, String>> generateLink(@PathVariable String roomId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        Room room = roomService.getRoomById(roomId);
        
        // Check if user is the owner or creator
        if (!room.getOwnerId().equals(userId) && !room.getCreators().contains(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String token = UUID.randomUUID().toString();
        roomService.saveRoomToken(roomId, token);
        
        return ResponseEntity.ok(Map.of("linkToken", token));
    }

    @DeleteMapping("/{roomId}/members/{memberId}")
    @ResponseBody
    public ResponseEntity<Void> removeMember(@PathVariable String roomId, @PathVariable String memberId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        Room room = roomService.getRoomById(roomId);
        
        // Check if user is the owner
        if (!room.getOwnerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        boolean removed = roomService.removeMember(roomId, memberId);
        
        if (removed) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{roomId}")
    @ResponseBody
    public ResponseEntity<Room> updateRoom(@PathVariable String roomId, @RequestBody Map<String, Object> updates) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        Room room = roomService.getRoomById(roomId);
        
        // Check if user is the owner
        if (!room.getOwnerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Update room name if provided
        String name = (String) updates.get("name");
        if (name != null && !name.trim().isEmpty()) {
            room.setName(name);
        }
        
        // Update access level if provided
        room.setAccessLevel(AccessLevel.valueOf(accessLevel));
        String accessLevel = (String) updates.get("accessLevel");
        if (accessLevel != null) {
            try {
                room.setAccessLevel(AccessLevel.valueOf(accessLevel));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        Room updatedRoom = roomService.updateRoom(room);
        return ResponseEntity.ok(updatedRoom);
    }

    @DeleteMapping("/{roomId}")
    @ResponseBody
    public ResponseEntity<Void> deleteRoom(@PathVariable String roomId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        Room room = roomService.getRoomById(roomId);
        
        // Check if user is the owner
        if (!room.getOwnerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        boolean deleted = roomService.deleteRoom(roomId);
        
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}