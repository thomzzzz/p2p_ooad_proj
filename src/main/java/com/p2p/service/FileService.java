package com.p2p.service;

import com.p2p.model.File;
import com.p2p.repository.FileRepository;
import com.p2p.util.Crypto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    
    private FileRepository fileRepository;
    private Crypto crypto;
    
    @Value("${file.upload.dir}")
    private String uploadDir;
    
    @Autowired
    public FileService(FileRepository fileRepository, Crypto crypto) {
        this.fileRepository = fileRepository;
        this.crypto = crypto;
    }
    
    public File storeFile(MultipartFile multipartFile, String ownerId, boolean encrypt) throws IOException {
        // Create the directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            logger.info("Created upload directory at: {}", uploadPath.toAbsolutePath());
        }
        
        // Generate a unique filename
        String originalFilename = multipartFile.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Set the file path
        Path filePath = uploadPath.resolve(newFilename);
        logger.debug("Saving file to: {}", filePath.toAbsolutePath());
        
        // Get file bytes
        byte[] fileBytes = multipartFile.getBytes();
        
        // Encrypt the file if requested
        if (encrypt) {
            try {
                SecretKey key = crypto.generateAESKey();
                fileBytes = crypto.encryptAES(fileBytes, key);
                
                // Save the key to a secure location or return to user
                // For now, we'll just print it (in a real system, this would be stored securely)
                String keyString = crypto.keyToString(key);
                logger.info("Encryption key for file {}: {}", newFilename, keyString);
            } catch (Exception e) {
                logger.error("Error encrypting file", e);
                throw new RuntimeException("Error encrypting file", e);
            }
        }
        
        // Save the file to the filesystem
        Files.write(filePath, fileBytes);
        logger.debug("File written to disk, size: {} bytes", fileBytes.length);
        
        // Create and save file metadata
        File file = new File();
        file.setFilename(newFilename);
        file.setOriginalFilename(originalFilename);
        file.setFilePath(filePath.toString());
        file.setFileType(multipartFile.getContentType());
        file.setFileSize(multipartFile.getSize());
        file.setOwnerId(ownerId);
        file.setUploadDate(new Date());
        file.setEncrypted(encrypt);
        
        File savedFile = fileRepository.save(file);
        logger.debug("File metadata saved to database with ID: {}", savedFile.getId());
        
        return savedFile;
    }
    
    public java.io.File getFile(String fileId) {
        File fileMetadata = fileRepository.findById(fileId)
                .orElseThrow(() -> {
                    logger.error("File metadata not found for ID: {}", fileId);
                    return new RuntimeException("File not found");
                });
        
        java.io.File file = new java.io.File(fileMetadata.getFilePath());
        if (!file.exists()) {
            logger.error("Physical file not found at: {}", fileMetadata.getFilePath());
        }
        
        return file;
    }
    
    public File getFileById(String fileId) {
        return fileRepository.findById(fileId)
                .orElse(null);
    }
    
    public List<File> getFilesByOwnerId(String ownerId) {
        return fileRepository.findByOwnerId(ownerId);
    }
    
    public void deleteFile(String fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        
        // Delete from filesystem
        try {
            boolean deleted = Files.deleteIfExists(Paths.get(file.getFilePath()));
            if (deleted) {
                logger.debug("Deleted file from disk: {}", file.getFilePath());
            } else {
                logger.warn("File not found on disk: {}", file.getFilePath());
            }
        } catch (IOException e) {
            logger.error("Error deleting file from disk", e);
            throw new RuntimeException("Error deleting file", e);
        }
        
        // Delete metadata
        fileRepository.delete(file);
        logger.debug("Deleted file metadata for ID: {}", fileId);
    }
    
    public byte[] decryptFile(byte[] encryptedFile, String keyString) {
        try {
            SecretKey key = crypto.stringToSecretKey(keyString);
            return crypto.decryptAES(encryptedFile, key);
        } catch (Exception e) {
            logger.error("Error decrypting file", e);
            throw new RuntimeException("Error decrypting file", e);
        }
    }
}