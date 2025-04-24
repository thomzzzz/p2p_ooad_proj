package com.p2p.controller;

import com.p2p.model.File;
import com.p2p.model.User;
import com.p2p.service.FileService;
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

    // Removed 'final' modifier
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
        
        File uploadedFile = fileService.storeFile(file, user.getId(), encrypt);
        return ResponseEntity.ok(uploadedFile);
    }
    
    @GetMapping
    public ResponseEntity<List<File>> getMyFiles(@AuthenticationPrincipal User user) {
        List<File> files = fileService.getFilesByOwnerId(user.getId());
        return ResponseEntity.ok(files);
    }
    
    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId, 
            @RequestParam(value = "decryptionKey", required = false) String decryptionKey) throws IOException {
        
        java.io.File file = fileService.getFile(fileId);
        Path path = Paths.get(file.getAbsolutePath());
        ByteArrayResource resource;
        
        byte[] fileData = Files.readAllBytes(path);
        
        // If decryption key is provided, decrypt the file
        if (decryptionKey != null && !decryptionKey.isEmpty()) {
            fileData = fileService.decryptFile(fileData, decryptionKey);
        }
        
        resource = new ByteArrayResource(fileData);
        
        // Get file metadata
        File fileMetadata = fileService.getFilesByOwnerId(null).stream()
                .filter(f -> f.getId().equals(fileId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("File not found"));
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileMetadata.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileMetadata.getOriginalFilename() + "\"")
                .body(resource);
    }
    
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileId, @AuthenticationPrincipal User user) {
        // Check if user owns the file
        File file = fileService.getFilesByOwnerId(user.getId()).stream()
                .filter(f -> f.getId().equals(fileId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("File not found or you don't have permission"));
        
        fileService.deleteFile(fileId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{fileId}/decrypt")
    public ResponseEntity<?> decryptFile(@PathVariable String fileId, @RequestBody Map<String, String> request) {
        String key = request.get("decryptionKey");
        // This would normally return the decrypted file or a status
        // For now, we just return a success message
        return ResponseEntity.ok().body("File decryption initiated");
    }
}