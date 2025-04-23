package com.p2pexchange.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.p2pexchange.model.FileDB;
import com.p2pexchange.model.TransferProgress;
import com.p2pexchange.model.TransferProgress.TransferState;
import com.p2pexchange.repository.FileRepository;
import com.p2pexchange.service.CryptoService;
import com.p2pexchange.exception.FileException;

/**
 * Service for file transfer operations with progress monitoring.
 * Implements the Strategy Pattern through FileUploader and FileDownloader interfaces.
 */
@Service
public class FileTransferService {
    
    /**
     * Interface for file uploading operations.
     */
    public interface FileUploader {
        String uploadFile(MultipartFile file, String userId) throws Exception;
        TransferProgress getUploadProgress(String transferId);
    }
    
    /**
     * Interface for file downloading operations.
     */
    public interface FileDownloader {
        Resource downloadFile(String fileId, String userId) throws Exception;
        TransferProgress getDownloadProgress(String transferId);
    }
    
    private final CryptoService cryptoService;
    private final FileRepository fileRepository;
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    // Transfer progress tracking
    private Map<String, TransferProgress> transferProgressMap = new ConcurrentHashMap<>();
    
    @Autowired
    public FileTransferService(CryptoService cryptoService, FileRepository fileRepository) {
        this.cryptoService = cryptoService;
        this.fileRepository = fileRepository;
    }
    
    /**
     * Initialize a transfer and return the transfer ID.
     * 
     * @param fileId The file ID
     * @param fileSize The file size in bytes
     * @return The transfer ID
     */
    public String initializeTransfer(String fileId, long fileSize) {
        String transferId = UUID.randomUUID().toString();
        transferProgressMap.put(transferId, new TransferProgress(fileId, fileSize));
        return transferId;
    }
    
    /**
     * Update transfer progress.
     * 
     * @param transferId The transfer ID
     * @param bytesTransferred The number of bytes transferred
     */
    public void updateProgress(String transferId, long bytesTransferred) {
        TransferProgress progress = transferProgressMap.get(transferId);
        if (progress != null) {
            progress.update(bytesTransferred);
        }
    }
    
    /**
     * Get transfer progress.
     * 
     * @param transferId The transfer ID
     * @return The transfer progress
     */
    public TransferProgress getProgress(String transferId) {
        return transferProgressMap.get(transferId);
    }
    
    /**
     * Encrypt file data before transfer.
     * 
     * @param fileData The file data to encrypt
     * @param algorithm The encryption algorithm to use
     * @param key The encryption key
     * @return The encrypted data
     * @throws Exception If encryption fails
     */
    public byte[] prepareForTransfer(byte[] fileData, String algorithm, String key) throws Exception {
        return cryptoService.encryptFile(fileData, algorithm, key);
    }
    
    /**
     * Decrypt received file data.
     * 
     * @param encryptedData The encrypted data
     * @param algorithm The encryption algorithm used
     * @param key The encryption key
     * @return The decrypted data
     * @throws Exception If decryption fails
     */
    public byte[] processReceivedFile(byte[] encryptedData, String algorithm, String key) throws Exception {
        return cryptoService.decryptFile(encryptedData, algorithm, key);
    }
    
    /**
     * Upload a file with progress tracking.
     * 
     * @param file The file to upload
     * @param userId The user ID of the uploader
     * @return The transfer ID
     * @throws Exception If the upload fails
     */
    public String uploadFile(MultipartFile file, String userId) throws Exception {
        // Create upload dir if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate a unique file name
        String uniqueFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(uniqueFileName);
        
        // Initialize transfer
        String transferId = initializeTransfer(uniqueFileName, file.getSize());
        TransferProgress progress = getProgress(transferId);
        
        try {
            // Read file data
            byte[] fileData = file.getBytes();
            
            // Encrypt file data (using AES by default)
            String encryptionKey = cryptoService.generateKey("AES");
            byte[] encryptedData = prepareForTransfer(fileData, "AES", encryptionKey);
            
            // Calculate checksum for original data
            String checksum = cryptoService.calculateChecksum(fileData);
            
            // Save file to disk
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
            fileDb.setEncryptionType("AES");
            
            // Store encryption key in metadata
            fileDb.addMetadata("encryptionKey", encryptionKey);
            
            // Save to database
            fileRepository.save(fileDb);
            
            // Update progress to complete
            progress.setBytesTransferred(file.getSize());
            progress.setState(TransferState.COMPLETED);
            
            return transferId;
        } catch (Exception e) {
            // Mark transfer as failed
            progress.setState(TransferState.FAILED);
            
            // Clean up partially written file
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException ioe) {
                // Log but don't rethrow
            }
            
            throw e;
        }
    }
    
    /**
     * Get upload progress.
     * 
     * @param transferId The transfer ID
     * @return The transfer progress
     */
    public TransferProgress getUploadProgress(String transferId) {
        return getProgress(transferId);
    }
    
    /**
     * Download a file with progress tracking.
     * 
     * @param fileId The file ID to download
     * @param userId The user ID of the downloader
     * @return The file as a Resource
     * @throws Exception If the download fails
     */
    public Resource downloadFile(String fileId, String userId) throws Exception {
        FileDB fileDb = fileRepository.findById(fileId)
                .orElseThrow(() -> FileException.fileNotFound(fileId));
        
        Path filePath = Paths.get(fileDb.getPath());
        
        if (!Files.exists(filePath)) {
            throw FileException.fileNotFound(fileId);
        }
        
        // Initialize transfer
        long fileSize = Files.size(filePath);
        String transferId = initializeTransfer(fileId, fileSize);
        TransferProgress progress = getProgress(transferId);
        
        try {
            Resource resource = new UrlResource(filePath.toUri());
            
            // Simulate progress updates (in a real implementation, this would be done during actual transfer)
            progress.update(fileSize);
            
            return resource;
        } catch (Exception e) {
            // Mark transfer as failed
            progress.setState(TransferState.FAILED);
            throw e;
        }
    }
    
    /**
     * Get download progress.
     * 
     * @param transferId The transfer ID
     * @return The transfer progress
     */
    public TransferProgress getDownloadProgress(String transferId) {
        return getProgress(transferId);
    }
    
    /**
     * Cancel a transfer.
     * 
     * @param transferId The transfer ID
     * @return True if the transfer was cancelled, false otherwise
     */
    public boolean cancelTransfer(String transferId) {
        TransferProgress progress = transferProgressMap.get(transferId);
        if (progress != null && progress.isActive()) {
            progress.cancel();
            return true;
        }
        return false;
    }
    
    /**
     * Pause a transfer.
     * 
     * @param transferId The transfer ID
     * @return True if the transfer was paused, false otherwise
     */
    public boolean pauseTransfer(String transferId) {
        TransferProgress progress = transferProgressMap.get(transferId);
        if (progress != null && progress.isActive()) {
            progress.pause();
            return true;
        }
        return false;
    }
    
    /**
     * Resume a transfer.
     * 
     * @param transferId The transfer ID
     * @return True if the transfer was resumed, false otherwise
     */
    public boolean resumeTransfer(String transferId) {
        TransferProgress progress = transferProgressMap.get(transferId);
        if (progress != null && progress.getState() == TransferState.PAUSED) {
            progress.resume();
            return true;
        }
        return false;
    }
    
    /**
     * Get a decrypted file.
     * 
     * @param fileId The file ID
     * @param userId The user ID requesting the file
     * @return The decrypted file data
     * @throws Exception If decryption fails
     */
    public byte[] getDecryptedFile(String fileId, String userId) throws Exception {
        FileDB fileDb = fileRepository.findById(fileId)
                .orElseThrow(() -> FileException.fileNotFound(fileId));
        
        // Check if user has access
        if (!fileDb.getOwnerId().equals(userId)) {
            throw FileException.accessDenied(userId, fileId);
        }
        
        Path filePath = Paths.get(fileDb.getPath());
        byte[] encryptedData = Files.readAllBytes(filePath);
        
        String encryptionType = fileDb.getEncryptionType();
        String encryptionKey = fileDb.getMetadata().get("encryptionKey");
        
        return processReceivedFile(encryptedData, encryptionType, encryptionKey);
    }
    
    /**
     * Clean up completed transfers that are older than a specified time.
     * 
     * @param maxAgeMillis The maximum age in milliseconds
     */
    public void cleanupOldTransfers(long maxAgeMillis) {
        long now = System.currentTimeMillis();
        
        transferProgressMap.entrySet().removeIf(entry -> {
            TransferProgress progress = entry.getValue();
            
            // Keep active transfers
            if (progress.isActive()) {
                return false;
            }
            
            // Remove old completed/failed/cancelled transfers
            long age = now - progress.getLastUpdateTime().getTime();
            return age > maxAgeMillis;
        });
    }
}