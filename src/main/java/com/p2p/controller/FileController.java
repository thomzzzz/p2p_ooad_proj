package com.p2p.controller;

import com.p2p.model.File;
import com.p2p.model.User;
import com.p2p.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private FileService fileService;
    
    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }
    
    @PostMapping("/upload")
    public ResponseEntity<File> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "encrypt", defaultValue = "false") boolean encrypt,
            @AuthenticationPrincipal User user) throws IOException {
        
        logger.debug("Uploading file: {}, encrypt: {}, user: {}", 
                file.getOriginalFilename(), encrypt, user.getUsername());
                
        if (file.isEmpty()) {
            logger.error("Upload failed: File is empty");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            File uploadedFile = fileService.storeFile(file, user.getId(), encrypt);
            logger.debug("File uploaded successfully. ID: {}", uploadedFile.getId());
            return ResponseEntity.ok(uploadedFile);
        } catch (Exception e) {
            logger.error("Error uploading file", e);
            throw e;
        }
    }
    
    @GetMapping
    public ResponseEntity<List<File>> getMyFiles(@AuthenticationPrincipal User user) {
        logger.debug("Getting files for user: {}", user.getUsername());
        List<File> files = fileService.getFilesByOwnerId(user.getId());
        return ResponseEntity.ok(files);
    }
    
    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId, 
            @RequestParam(value = "decryptionKey", required = false) String decryptionKey) throws IOException {
        
        logger.debug("Downloading file: {}", fileId);
        
        try {
            java.io.File file = fileService.getFile(fileId);
            if (!file.exists()) {
                logger.error("File not found on disk: {}", file.getAbsolutePath());
                return ResponseEntity.notFound().build();
            }
            
            Path path = Paths.get(file.getAbsolutePath());
            ByteArrayResource resource;
            
            byte[] fileData = Files.readAllBytes(path);
            
            // If decryption key is provided, decrypt the file
            if (decryptionKey != null && !decryptionKey.isEmpty()) {
                logger.debug("Decrypting file with provided key");
                fileData = fileService.decryptFile(fileData, decryptionKey);
            }
            
            resource = new ByteArrayResource(fileData);
            
            // Get file metadata
            File fileMetadata = fileService.getFileById(fileId);
            
            if (fileMetadata == null) {
                logger.error("File metadata not found for ID: {}", fileId);
                return ResponseEntity.notFound().build();
            }
            
            logger.debug("Serving file: {}, type: {}", fileMetadata.getOriginalFilename(), fileMetadata.getFileType());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileMetadata.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileMetadata.getOriginalFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error downloading file", e);
            throw e;
        }
    }
    
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileId, @AuthenticationPrincipal User user) {
        logger.debug("Deleting file: {}", fileId);
        
        // Check if user owns the file
        File file = fileService.getFilesByOwnerId(user.getId()).stream()
                .filter(f -> f.getId().equals(fileId))
                .findFirst()
                .orElse(null);
                
        if (file == null) {
            logger.error("File not found or user doesn't have permission");
            return ResponseEntity.notFound().build();
        }
        
        fileService.deleteFile(fileId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{fileId}/decrypt")
    public ResponseEntity<?> decryptFile(@PathVariable String fileId, @RequestBody Map<String, String> request) {
        String key = request.get("decryptionKey");
        logger.debug("Decryption requested for file: {}", fileId);
        // This would normally return the decrypted file or a status
        // For now, we just return a success message
        return ResponseEntity.ok().body("File decryption initiated");
    }
}