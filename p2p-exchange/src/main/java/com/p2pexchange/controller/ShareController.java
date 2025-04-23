package com.p2pexchange.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.p2pexchange.model.File;
import com.p2pexchange.model.Room;
import com.p2pexchange.model.TransferProgress;
import com.p2pexchange.service.FileService;
import com.p2pexchange.service.FileTransferService;
import com.p2pexchange.service.RoomService;
import com.p2pexchange.service.UserService;

@RestController
@RequestMapping("/api/share")
public class ShareController {

    private final FileService fileService;
    private final RoomService roomService;
    private final UserService userService;
    private final FileTransferService fileTransferService;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    public ShareController(FileService fileService, RoomService roomService, 
                          UserService userService, FileTransferService fileTransferService,
                          SimpMessagingTemplate messagingTemplate) {
        this.fileService = fileService;
        this.roomService = roomService;
        this.userService = userService;
        this.fileTransferService = fileTransferService;
        this.messagingTemplate = messagingTemplate;
    }
    
    @PostMapping("/upload/{roomId}")
    public ResponseEntity<String> uploadToRoom(@PathVariable String roomId,
                                            @RequestParam("file") MultipartFile file,
                                            @RequestParam(value = "encryptionType", defaultValue = "AES") String encryptionType) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        // Check if user has access to the room
        Room room = roomService.getRoomById(roomId);
        if (!room.hasAccess(userId)) {
            return ResponseEntity.status(403).body("Access denied to this room");
        }
        
        // Check if user can share files in this room
        if (!room.canShareFiles(userId)) {
            return ResponseEntity.status(403).body("You don't have permission to share files in this room");
        }
        
        try {
            // Initialize transfer tracking
            String transferId = UUID.randomUUID().toString();
            fileTransferService.initializeTransfer(transferId, file.getSize());
            
            // Upload file
            File savedFile = fileService.storeFile(file, userId, encryptionType);
            
            // Add file to room
            roomService.addFileToRoom(roomId, savedFile.getId());
            
            // Notify room members
            String username = userService.getUserById(userId).getUsername();
            Map<String, Object> notification = Map.of(
                "type", "FILE_SHARED",
                "data", Map.of(
                    "filename", savedFile.getFilename(),
                    "fileId", savedFile.getId(),
                    "sharedBy", username,
                    "fileSize", savedFile.getFileSize(),
                    "fileType", savedFile.getFileType()
                )
            );
            messagingTemplate.convertAndSend("/topic/room/" + roomId, notification);
            
            return ResponseEntity.ok(transferId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading file: " + e.getMessage());
        }
    }
    
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        // Check if user has access to this file
        if (!fileService.hasAccess(fileId, userId)) {
            return ResponseEntity.status(403).build();
        }
        
        // Get file info
        File file = fileService.getFileById(fileId);
        
        // Initialize transfer tracking
        String transferId = UUID.randomUUID().toString();
        fileTransferService.initializeTransfer(transferId, file.getFileSize());
        
        // Get file resource
        Resource resource = fileService.loadFileAsResource(fileId);
        
        // Notify about download
        String username = userService.getUserById(userId).getUsername();
        Map<String, Object> notification = Map.of(
            "type", "FILE_DOWNLOADED",
            "data", Map.of(
                "filename", file.getFilename(),
                "downloadedBy", username
            )
        );
        
        // If file is in a room, notify room members
        for (Room room : roomService.getRoomsContainingFile(fileId)) {
            messagingTemplate.convertAndSend("/topic/room/" + room.getId(), notification);
        }
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(resource);
    }
    
    @GetMapping("/initDownload/{fileId}")
    public ResponseEntity<Map<String, String>> initializeDownload(@PathVariable String fileId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        // Check if user has access to this file
        if (!fileService.hasAccess(fileId, userId)) {
            return ResponseEntity.status(403).build();
        }
        
        File file = fileService.getFileById(fileId);
        String transferId = fileTransferService.initializeTransfer(fileId, file.getFileSize());
        
        return ResponseEntity.ok(Map.of("transferId", transferId));
    }
    
    @GetMapping("/progress/{transferId}")
    public ResponseEntity<TransferProgress> getTransferProgress(@PathVariable String transferId) {
        TransferProgress progress = fileTransferService.getProgress(transferId);
        
        if (progress == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(progress);
    }
}