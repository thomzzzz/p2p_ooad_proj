package com.p2pexchange.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.p2pexchange.model.User;
import com.p2pexchange.model.File;
import com.p2pexchange.model.Room;
import com.p2pexchange.service.RoomService.RoomEvent;
import com.p2pexchange.service.RoomService.RoomEventType;
import com.p2pexchange.service.RoomService.RoomObserver;
import com.p2pexchange.service.UserService;
import com.p2pexchange.service.FileService;

/**
 * Service for real-time notifications implementing the Observer pattern.
 */
@Service
public class NotificationService implements RoomObserver {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final FileService fileService;
    
    @Autowired
    public NotificationService(SimpMessagingTemplate messagingTemplate, 
                              UserService userService,
                              FileService fileService,
                              RoomService roomService) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
        this.fileService = fileService;
        
        // Register as observer
        roomService.addObserver(this);
    }
    
    // RoomObserver implementation
    @Override
    public void onRoomEvent(RoomEvent event) {
        switch (event.getType()) {
            case ROOM_CREATED:
                notifyRoomCreated(event.getRoomId(), event.getUserId());
                break;
            case USER_JOINED:
                notifyUserJoined(event.getRoomId(), event.getUserId());
                break;
            case USER_LEFT:
                notifyUserLeft(event.getRoomId(), event.getUserId());
                break;
            case FILE_SHARED:
                notifyFileShared(event.getRoomId(), event.getFileId(), event.getUserId());
                break;
            case ROOM_DELETED:
                notifyRoomDeleted(event.getRoomId());
                break;
        }
    }
    
    // Notification methods
    private void notifyRoomCreated(String roomId, String userId) {
        User user = userService.getUserById(userId);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ROOM_CREATED");
        notification.put("data", Map.of(
            "roomId", roomId,
            "createdBy", user.getUsername()
        ));
        
        // Send to all users (global notification)
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }
    
    private void notifyUserJoined(String roomId, String userId) {
        User user = userService.getUserById(userId);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "USER_JOINED");
        notification.put("data", Map.of(
            "roomId", roomId,
            "userId", userId,
            "username", user.getUsername()
        ));
        
        // Send to room members
        messagingTemplate.convertAndSend("/topic/room/" + roomId, notification);
    }
    
    private void notifyUserLeft(String roomId, String userId) {
        User user = userService.getUserById(userId);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "USER_LEFT");
        notification.put("data", Map.of(
            "roomId", roomId,
            "userId", userId,
            "username", user.getUsername()
        ));
        
        // Send to room members
        messagingTemplate.convertAndSend("/topic/room/" + roomId, notification);
    }
    
    private void notifyFileShared(String roomId, String fileId, String userId) {
        User user = userService.getUserById(userId);
        File file = fileService.getFileById(fileId);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "FILE_SHARED");
        notification.put("data", Map.of(
            "roomId", roomId,
            "fileId", fileId,
            "filename", file.getFilename(),
            "fileSize", file.getFileSize(),
            "fileType", file.getFileType(),
            "sharedBy", user.getUsername()
        ));
        
        // Send to room members
        messagingTemplate.convertAndSend("/topic/room/" + roomId, notification);
    }
    
    private void notifyRoomDeleted(String roomId) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ROOM_DELETED");
        notification.put("data", Map.of(
            "roomId", roomId
        ));
        
        // Send to room members
        messagingTemplate.convertAndSend("/topic/room/" + roomId, notification);
    }
    
    // Additional notification methods
    public void sendDirectNotification(String userId, String type, Map<String, Object> data) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", type);
        notification.put("data", data);
        
        // Send to specific user
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification);
    }
    
    public void sendAdminNotification(String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ADMIN_NOTIFICATION");
        notification.put("data", Map.of(
            "message", message,
            "timestamp", System.currentTimeMillis()
        ));
        
        // Send to users with admin role
        messagingTemplate.convertAndSend("/topic/admin", notification);
    }
    
    public void notifyFileDownloaded(String fileId, String userId) {
        User user = userService.getUserById(userId);
        File file = fileService.getFileById(fileId);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "FILE_DOWNLOADED");
        notification.put("data", Map.of(
            "fileId", fileId,
            "filename", file.getFilename(),
            "downloadedBy", user.getUsername()
        ));
        
        // Notify file owner
        messagingTemplate.convertAndSendToUser(file.getOwner(), "/queue/notifications", notification);
    }
}