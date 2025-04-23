package com.p2pexchange.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.p2pexchange.model.File;
import com.p2pexchange.model.FileDB;
import com.p2pexchange.repository.FileRepository;
import com.p2pexchange.exception.FileException;

/**
 * Service for file management operations using Factory Method Pattern.
 */
@Service
public class FileService {
    
    private final FileRepository fileRepository;
    private final CryptoService cryptoService;
    private final RoomService roomService;
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Value("${file.max-size:104857600}") // Default: 100MB
    private long maxFileSize;
    
    @Autowired
    public FileService(FileRepository fileRepository, CryptoService cryptoService, RoomService roomService) {
        this.fileRepository = fileRepository;
        this.cryptoService = cryptoService;
        this.roomService = roomService;
    }
    
    // Factory Method Pattern
    public abstract class FileHandler {
        protected abstract File processFile(MultipartFile rawFile, String userId) throws Exception;
        
        public File handleFile(MultipartFile rawFile, String userId) throws Exception {
            // Validate file
            if (rawFile.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }
            
            if (rawFile.getSize() > maxFileSize) {
                throw new IllegalArgumentException("File exceeds maximum size limit of " + (maxFileSize / 1024 / 1024) + "MB");
            }
            
            // Process file based on specific handler
            return processFile(rawFile, userId);
        }
    }
    
    // Concrete handler for image files
    public class ImageFileHandler extends FileHandler {
        @Override
        protected File processFile(MultipartFile rawFile, String userId) throws Exception {
            // Image-specific validations
            String contentType = rawFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Invalid image file type");
            }
            
            // Save the file
            return saveFile(rawFile, userId, "AES"); // Default to AES encryption for images
        }
    }
    
    // Concrete handler for document files
    public class DocumentFileHandler extends FileHandler {
        @Override
        protected File processFile(MultipartFile rawFile, String userId) throws Exception {
            // Document-specific validations
            String contentType = rawFile.getContentType();
            if (contentType == null || 
                !(contentType.startsWith("application/pdf") || 
                  contentType.startsWith("application/msword") || 
                  contentType.startsWith("application/vnd.openxmlformats-officedocument") ||
                  contentType.startsWith("text/plain") ||
                  contentType.startsWith("application/vnd.ms-excel") ||
                  contentType.startsWith("application/vnd.ms-powerpoint"))) {
                throw new IllegalArgumentException("Invalid document file type");
            }
            
            // Save the file
            return saveFile(rawFile, userId, "AES"); // Default to AES encryption for documents
        }
    }
    
    // Concrete handler for archive files
    public class ArchiveFileHandler extends FileHandler {
        @Override
        protected File processFile(MultipartFile rawFile, String userId) throws Exception {
            // Archive-specific validations
            String contentType = rawFile.getContentType();
            if (contentType == null || 
                !(contentType.startsWith("application/zip") || 
                  contentType.startsWith("application/x-rar-compressed") || 
                  contentType.equals("application/x-tar") ||
                  contentType.equals("application/gzip"))) {
                throw new IllegalArgumentException("Invalid archive file type");
            }
            
            // Save the file
            return saveFile(rawFile, userId, "AES"); // Default to AES encryption for archives
        }
    }
    
    // Default handler for other file types
    public class DefaultFileHandler extends FileHandler {
        @Override
        protected File processFile(MultipartFile rawFile, String userId) throws Exception {
            // Generic validations
            if (rawFile.getContentType() == null) {
                throw new IllegalArgumentException("Unknown file type");
            }
            
            // Save the file
            return saveFile(rawFile, userId, "AES"); // Default to AES encryption
        }
    }
    
    // Factory method for getting appropriate handler
    public FileHandler getHandlerForFileType(String fileType) {
        if (fileType == null) {
            return new DefaultFileHandler();
        }
        
        if (fileType.startsWith("image/")) {
            return new ImageFileHandler();
        } else if (fileType.startsWith("application/pdf") || 
                  fileType.startsWith("application/msword") || 
                  fileType.startsWith("application/vnd.openxmlformats-officedocument") ||
                  fileType.startsWith("text/plain") ||
                  fileType.startsWith("application/vnd.ms-excel") ||
                  fileType.startsWith("application/vnd.ms-powerpoint")) {
            return new DocumentFileHandler();
        } else if (fileType.startsWith("application/zip") || 
                  fileType.startsWith("application/x-rar-compressed") || 
                  fileType.equals("application/x-tar") ||
                  fileType.equals("application/gzip")) {
            return new ArchiveFileHandler();
        } else {
            return new DefaultFileHandler();
        }
    }
    
    // File storage methods
    public File storeFile(MultipartFile file, String userId, String encryptionType) throws Exception {
        FileHandler handler = getHandlerForFileType(file.getContentType());
        return handler.handleFile(file, userId);
    }
    
    private File saveFile(MultipartFile file, String userId, String encryptionType) throws Exception {
        // Create upload dir if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(uniqueFileName);
        
        // Read original file bytes
        byte[] fileBytes = file.getBytes();
        
        // Encrypt file data
        String encryptionKey = cryptoService.generateKey(encryptionType);
        byte[] encryptedData = cryptoService.encryptFile(fileBytes, encryptionType, encryptionKey);
        
        // Calculate checksum from original data
        String checksum = cryptoService.calculateChecksum(fileBytes);
        
        // Write encrypted data to file
        Files.write(filePath, encryptedData);
        
        // Create file metadata entity
        FileDB fileDb = new FileDB();
        fileDb.setFilename(file.getOriginalFilename());
        fileDb.setOriginalFilename(file.getOriginalFilename());
        fileDb.setContentType(file.getContentType());
        fileDb.setSize(file.getSize());
        fileDb.setPath(filePath.toString());
        fileDb.setOwnerId(userId);
        fileDb.setChecksum(checksum);
        fileDb.setEncryptionType(encryptionType);
        
        // Store encryption key in metadata
        fileDb.addMetadata("encryptionKey", encryptionKey);
        
        // Save to database
        FileDB savedFile = fileRepository.save(fileDb);
        
        return savedFile.toFileModel();
    }
    
    // File retrieval methods
    public Resource loadFileAsResource(String fileId) {
        try {
            FileDB fileDb = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found with id: " + fileId));
            
            Path filePath = Paths.get(fileDb.getPath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new IOException("File not found: " + fileDb.getPath());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load file", e);
        }
    }
    
    public byte[] loadDecryptedFile(String fileId) {
        try {
            FileDB fileDb = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found with id: " + fileId));
            
            Path filePath = Paths.get(fileDb.getPath());
            byte[] encryptedData = Files.readAllBytes(filePath);
            
            String encryptionType = fileDb.getEncryptionType();
            String encryptionKey = fileDb.getMetadata().get("encryptionKey");
            
            return cryptoService.decryptFile(encryptedData, encryptionType, encryptionKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load and decrypt file", e);
        }
    }
    
    // File listing methods
    public List<File> getFilesByUser(String userId) {
        List<FileDB> fileDbList = fileRepository.findByOwnerId(userId);
        return fileDbList.stream()
            .map(FileDB::toFileModel)
            .collect(Collectors.toList());
    }
    
    public List<File> getFilesByIds(List<String> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        Iterable<FileDB> fileDbIterable = fileRepository.findAllById(fileIds);
        List<FileDB> fileDbList = new ArrayList<>();
        fileDbIterable.forEach(fileDbList::add);
        
        return fileDbList.stream()
            .map(FileDB::toFileModel)
            .collect(Collectors.toList());
    }
    
    public File getFileById(String fileId) {
        FileDB fileDb = fileRepository.findById(fileId)
            .orElseThrow(() -> new IllegalArgumentException("File not found with id: " + fileId));
        
        return fileDb.toFileModel();
    }
    
    // File search methods
    public List<File> searchFiles(String query, String userId) {
        List<FileDB> fileDbList = fileRepository.findByNameContainingAndOwnerId(query, userId);
        return fileDbList.stream()
            .map(FileDB::toFileModel)
            .collect(Collectors.toList());
    }
    
    // File operation methods
    public boolean deleteFile(String fileId) {
        try {
            FileDB fileDb = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found with id: " + fileId));
            
            // Delete physical file
            Path filePath = Paths.get(fileDb.getPath());
            Files.deleteIfExists(filePath);
            
            // Remove file from rooms
            roomService.removeFileFromAllRooms(fileId);
            
            // Delete from database
            fileRepository.deleteById(fileId);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Access control
    public boolean hasAccess(String fileId, String userId) {
        FileDB fileDb = fileRepository.findById(fileId).orElse(null);
        if (fileDb == null) {
            return false;
        }
        
        // Owner has access
        if (fileDb.getOwnerId().equals(userId)) {
            return true;
        }
        
        // Check if file is shared in a room where user is a member
        return roomService.canAccessFile(fileId, userId);
    }
}