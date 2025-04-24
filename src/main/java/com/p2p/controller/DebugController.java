package com.p2p.controller;

import com.p2p.model.File;
import com.p2p.model.Room;
import com.p2p.model.User;
import com.p2p.repository.FileRepository;
import com.p2p.repository.RoomRepository;
import com.p2p.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);
    
    @Value("${file.upload.dir}")
    private String uploadDir;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private FileRepository fileRepository;
    
    @GetMapping("/system-info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        
        // System info
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("osName", System.getProperty("os.name"));
        info.put("uploadDirectory", uploadDir);
        
        // Check if upload directory exists
        Path uploadPath = Paths.get(uploadDir);
        boolean uploadDirExists = Files.exists(uploadPath);
        info.put("uploadDirectoryExists", uploadDirExists);
        
        if (uploadDirExists) {
            try {
                info.put("uploadDirectoryIsReadable", Files.isReadable(uploadPath));
                info.put("uploadDirectoryIsWritable", Files.isWritable(uploadPath));
                info.put("uploadDirectoryAbsolutePath", uploadPath.toAbsolutePath().toString());
            } catch (Exception e) {
                logger.error("Error checking upload directory", e);
                info.put("error", e.getMessage());
            }
        }
        
        // Database stats
        try {
            long userCount = userRepository.count();
            long roomCount = roomRepository.count();
            long fileCount = fileRepository.count();
            
            Map<String, Long> dbStats = new HashMap<>();
            dbStats.put("users", userCount);
            dbStats.put("rooms", roomCount);
            dbStats.put("files", fileCount);
            
            info.put("databaseStats", dbStats);
        } catch (Exception e) {
            logger.error("Error getting database stats", e);
            info.put("databaseError", e.getMessage());
        }
        
        return ResponseEntity.ok(info);
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
    
    @GetMapping("/rooms")
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomRepository.findAll());
    }
    
    @GetMapping("/files")
    public ResponseEntity<List<File>> getAllFiles() {
        return ResponseEntity.ok(fileRepository.findAll());
    }
    
    @GetMapping("/rooms/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserRooms(@PathVariable String userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                result.put("error", "User not found");
                return ResponseEntity.ok(result);
            }
            
            result.put("userId", userId);
            result.put("username", user.getUsername());
            
            // Rooms where user is creator
            List<Room> createdRooms = roomRepository.findByCreatorId(userId);
            result.put("createdRoomsCount", createdRooms.size());
            result.put("createdRooms", createdRooms);
            
            // Rooms where user is a member
            List<Room> memberRooms = roomRepository.findByMembersContains(userId);
            result.put("memberRoomsCount", memberRooms.size());
            result.put("memberRooms", memberRooms);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error getting user rooms", e);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    @GetMapping("/file/{fileId}")
    public ResponseEntity<Map<String, Object>> getFileDetails(@PathVariable String fileId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            File file = fileRepository.findById(fileId).orElse(null);
            if (file == null) {
                result.put("error", "File not found in database");
                return ResponseEntity.ok(result);
            }
            
            result.put("fileId", file.getId());
            result.put("filename", file.getFilename());
            result.put("originalFilename", file.getOriginalFilename());
            result.put("filePath", file.getFilePath());
            result.put("fileType", file.getFileType());
            result.put("fileSize", file.getFileSize());
            result.put("ownerId", file.getOwnerId());
            result.put("uploadDate", file.getUploadDate());
            result.put("encrypted", file.isEncrypted());
            
            // Check if file exists on disk
            java.io.File physicalFile = new java.io.File(file.getFilePath());
            result.put("existsOnDisk", physicalFile.exists());
            
            if (physicalFile.exists()) {
                result.put("actualFileSize", physicalFile.length());
                result.put("canRead", physicalFile.canRead());
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error getting file details", e);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}
