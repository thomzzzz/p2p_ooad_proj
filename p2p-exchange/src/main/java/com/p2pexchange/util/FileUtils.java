package com.p2pexchange.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

/**
 * Utility class for file operations.
 */
public class FileUtils {

    /**
     * Save a MultipartFile to disk with a random filename.
     * 
     * @param file The file to save
     * @param uploadDir The directory to save the file in
     * @return The path to the saved file
     * @throws IOException If an I/O error occurs
     */
    public static String saveFile(MultipartFile file, String uploadDir) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        
        // Create directories if they don't exist
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(uniqueFileName);
        
        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return filePath.toString();
    }
    
    /**
     * Get a file's content type based on its extension.
     * 
     * @param fileName The file name
     * @return The content type, or "application/octet-stream" if unknown
     */
    public static String getContentType(String fileName) {
        String extension = getFileExtension(fileName);
        
        if (extension == null) {
            return "application/octet-stream";
        }
        
        switch (extension.toLowerCase()) {
            case "txt":
                return "text/plain";
            case "html":
            case "htm":
                return "text/html";
            case "css":
                return "text/css";
            case "js":
                return "application/javascript";
            case "json":
                return "application/json";
            case "xml":
                return "application/xml";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "zip":
                return "application/zip";
            case "rar":
                return "application/x-rar-compressed";
            case "tar":
                return "application/x-tar";
            case "gz":
                return "application/gzip";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "svg":
                return "image/svg+xml";
            case "mp3":
                return "audio/mpeg";
            case "mp4":
                return "video/mp4";
            default:
                return "application/octet-stream";
        }
    }
    
    /**
     * Get a file's extension.
     * 
     * @param fileName The file name
     * @return The file extension, or null if none
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return null;
        }
        
        return fileName.substring(lastDotIndex + 1);
    }
    
    /**
     * Format a file size for display.
     * 
     * @param sizeInBytes The file size in bytes
     * @return The formatted file size (e.g., "2.5 MB")
     */
    public static String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " B";
        } else if (sizeInBytes < 1024 * 1024) {
            return String.format("%.1f KB", sizeInBytes / 1024.0);
        } else if (sizeInBytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", sizeInBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", sizeInBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * Check if a file is an image based on its content type.
     * 
     * @param contentType The file's content type
     * @return True if the file is an image, false otherwise
     */
    public static boolean isImage(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }
    
    /**
     * Check if a file is a document based on its content type.
     * 
     * @param contentType The file's content type
     * @return True if the file is a document, false otherwise
     */
    public static boolean isDocument(String contentType) {
        if (contentType == null) {
            return false;
        }
        
        return contentType.equals("application/pdf") ||
               contentType.equals("application/msword") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
               contentType.equals("application/vnd.ms-excel") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
               contentType.equals("application/vnd.ms-powerpoint") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation") ||
               contentType.startsWith("text/");
    }
    
    /**
     * Check if a file is an archive based on its content type.
     * 
     * @param contentType The file's content type
     * @return True if the file is an archive, false otherwise
     */
    public static boolean isArchive(String contentType) {
        if (contentType == null) {
            return false;
        }
        
        return contentType.equals("application/zip") ||
               contentType.equals("application/x-rar-compressed") ||
               contentType.equals("application/x-tar") ||
               contentType.equals("application/gzip");
    }
    
    /**
     * Calculate a file's checksum using SHA-256.
     * 
     * @param filePath The path to the file
     * @return The checksum as a hex string
     * @throws IOException If an I/O error occurs
     */
    public static String calculateChecksum(Path filePath) throws IOException {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] digest = md.digest(fileBytes);
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
    
    /**
     * Split a large file into smaller chunks.
     * 
     * @param filePath The path to the file
     * @param chunkSize The size of each chunk in bytes
     * @return An array of paths to the chunk files
     * @throws IOException If an I/O error occurs
     */
    public static Path[] splitFile(Path filePath, int chunkSize) throws IOException {
        byte[] buffer = new byte[chunkSize];
        long fileSize = Files.size(filePath);
        int numChunks = (int) Math.ceil((double) fileSize / chunkSize);
        Path[] chunkPaths = new Path[numChunks];
        
        try (java.io.InputStream is = Files.newInputStream(filePath)) {
            for (int i = 0; i < numChunks; i++) {
                Path chunkPath = filePath.getParent().resolve(filePath.getFileName() + ".part" + i);
                chunkPaths[i] = chunkPath;
                
                try (java.io.OutputStream os = Files.newOutputStream(chunkPath)) {
                    int bytesRead = is.read(buffer);
                    if (bytesRead != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
        
        return chunkPaths;
    }
    
    /**
     * Merge file chunks back into a single file.
     * 
     * @param chunkPaths The paths to the chunk files
     * @param outputPath The path for the merged file
     * @throws IOException If an I/O error occurs
     */
    public static void mergeChunks(Path[] chunkPaths, Path outputPath) throws IOException {
        try (java.io.OutputStream os = Files.newOutputStream(outputPath)) {
            byte[] buffer = new byte[8192];
            
            for (Path chunkPath : chunkPaths) {
                try (java.io.InputStream is = Files.newInputStream(chunkPath)) {
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
                
                // Delete chunk after merging
                Files.delete(chunkPath);
            }
        }
    }
}