package com.p2p.service;

import com.p2p.model.File;
import com.p2p.repository.FileRepository;
import com.p2p.util.Crypto;
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

    // Removed 'final' modifiers
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
        }
        
        // Generate a unique filename
        String originalFilename = multipartFile.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Set the file path
        Path filePath = uploadPath.resolve(newFilename);
        
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
                System.out.println("Encryption key: " + keyString);
            } catch (Exception e) {
                throw new RuntimeException("Error encrypting file", e);
            }
        }
        
        // Save the file to the filesystem
        Files.write(filePath, fileBytes);
        
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
        
        return fileRepository.save(file);
    }
    
    public java.io.File getFile(String fileId) {
        File fileMetadata = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        
        return new java.io.File(fileMetadata.getFilePath());
    }
    
    public List<File> getFilesByOwnerId(String ownerId) {
        return fileRepository.findByOwnerId(ownerId);
    }
    
    public void deleteFile(String fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        
        // Delete from filesystem
        try {
            Files.deleteIfExists(Paths.get(file.getFilePath()));
        } catch (IOException e) {
            throw new RuntimeException("Error deleting file", e);
        }
        
        // Delete metadata
        fileRepository.delete(file);
    }
    
    public byte[] decryptFile(byte[] encryptedFile, String keyString) {
        try {
            SecretKey key = crypto.stringToSecretKey(keyString);
            return crypto.decryptAES(encryptedFile, key);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting file", e);
        }
    }
}