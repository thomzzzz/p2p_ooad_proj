package com.p2p.controller;

import com.p2p.model.File;
import com.p2p.model.Room;
import com.p2p.model.WebSocketMessage;
import com.p2p.service.FileService;
import com.p2p.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketController {

    // Removed 'final' modifiers
    private RoomService roomService;
    private FileService fileService;
    
    @Autowired
    public WebSocketController(RoomService roomService, FileService fileService) {
        this.roomService = roomService;
        this.fileService = fileService;
    }
    
    @MessageMapping("/room.join/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public WebSocketMessage joinRoom(@DestinationVariable String roomId, 
                                    @Payload WebSocketMessage message,
                                    SimpMessageHeaderAccessor headerAccessor,
                                    Principal principal) {
        
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", principal.getName());
        headerAccessor.getSessionAttributes().put("roomId", roomId);
        
        // Get room details
        Room room = roomService.getRoomById(roomId);
        
        // Add message metadata
        message.setType(WebSocketMessage.MessageType.JOIN);
        message.setSender(principal.getName());
        message.setContent(principal.getName() + " joined the room");
        
        return message;
    }
    
    @MessageMapping("/room.leave/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public WebSocketMessage leaveRoom(@DestinationVariable String roomId,
                                     @Payload WebSocketMessage message,
                                     SimpMessageHeaderAccessor headerAccessor,
                                     Principal principal) {
        
        message.setType(WebSocketMessage.MessageType.LEAVE);
        message.setSender(principal.getName());
        message.setContent(principal.getName() + " left the room");
        
        return message;
    }
    
    @MessageMapping("/room.chat/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public WebSocketMessage sendMessage(@DestinationVariable String roomId,
                                       @Payload WebSocketMessage message,
                                       Principal principal) {
        
        message.setType(WebSocketMessage.MessageType.CHAT);
        message.setSender(principal.getName());
        
        return message;
    }
    
    @MessageMapping("/room.file/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public WebSocketMessage shareFile(@DestinationVariable String roomId,
                                     @Payload WebSocketMessage message,
                                     Principal principal) {
        
        // Get file information
        String fileId = message.getContent();
        File file = fileService.getFilesByOwnerId(principal.getName()).stream()
            .filter(f -> f.getId().equals(fileId))
            .findFirst()
            .orElse(null);
            
        if (file != null) {
            // Add file to room
            roomService.addFileToRoom(roomId, fileId);
            
            message.setType(WebSocketMessage.MessageType.FILE);
            message.setSender(principal.getName());
            message.setContent(principal.getName() + " shared a file: " + file.getOriginalFilename());
            message.setFileId(fileId);
        }
        
        return message;
    }
}