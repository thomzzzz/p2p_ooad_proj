package com.p2pexchange.exception;

/**
 * Exception for file-related errors.
 */
public class FileException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final FileErrorCode errorCode;
    
    /**
     * Error codes for file operations.
     */
    public enum FileErrorCode {
        FILE_NOT_FOUND,
        FILE_STORAGE_ERROR,
        FILE_ACCESS_DENIED,
        FILE_SIZE_EXCEEDED,
        FILE_TYPE_NOT_SUPPORTED,
        FILE_ENCRYPTION_ERROR,
        FILE_DECRYPTION_ERROR,
        FILE_TRANSFER_ERROR,
        INVALID_FILE_METADATA,
        FILE_CHECKSUM_MISMATCH
    }
    
    /**
     * Constructor with message and error code.
     * 
     * @param message The error message
     * @param errorCode The error code
     */
    public FileException(String message, FileErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructor with message, cause, and error code.
     * 
     * @param message The error message
     * @param cause The cause of the error
     * @param errorCode The error code
     */
    public FileException(String message, Throwable cause, FileErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Get the error code.
     * 
     * @return The error code
     */
    public FileErrorCode getErrorCode() {
        return errorCode;
    }
    
    /**
     * Create a FILE_NOT_FOUND exception.
     * 
     * @param fileId The ID of the file that was not found
     * @return The exception
     */
    public static FileException fileNotFound(String fileId) {
        return new FileException("File not found with ID: " + fileId, FileErrorCode.FILE_NOT_FOUND);
    }
    
    /**
     * Create a FILE_STORAGE_ERROR exception.
     * 
     * @param message The error message
     * @param cause The cause of the error
     * @return The exception
     */
    public static FileException storageError(String message, Throwable cause) {
        return new FileException("File storage error: " + message, cause, FileErrorCode.FILE_STORAGE_ERROR);
    }
    
    /**
     * Create a FILE_ACCESS_DENIED exception.
     * 
     * @param userId The ID of the user who was denied access
     * @param fileId The ID of the file that was accessed
     * @return The exception
     */
    public static FileException accessDenied(String userId, String fileId) {
        return new FileException("Access denied for user " + userId + " to file " + fileId, FileErrorCode.FILE_ACCESS_DENIED);
    }
    
    /**
     * Create a FILE_SIZE_EXCEEDED exception.
     * 
     * @param fileName The name of the file
     * @param fileSize The size of the file
     * @param maxSize The maximum allowed size
     * @return The exception
     */
    public static FileException fileSizeExceeded(String fileName, long fileSize, long maxSize) {
        return new FileException(
            "File size exceeded: " + fileName + " (" + fileSize + " bytes), maximum allowed: " + maxSize + " bytes",
            FileErrorCode.FILE_SIZE_EXCEEDED
        );
    }
    
    /**
     * Create a FILE_TYPE_NOT_SUPPORTED exception.
     * 
     * @param fileName The name of the file
     * @param contentType The content type of the file
     * @return The exception
     */
    public static FileException fileTypeNotSupported(String fileName, String contentType) {
        return new FileException(
            "File type not supported: " + fileName + " (" + contentType + ")",
            FileErrorCode.FILE_TYPE_NOT_SUPPORTED
        );
    }
    
    /**
     * Create a FILE_ENCRYPTION_ERROR exception.
     * 
     * @param message The error message
     * @param cause The cause of the error
     * @return The exception
     */
    public static FileException encryptionError(String message, Throwable cause) {
        return new FileException("File encryption error: " + message, cause, FileErrorCode.FILE_ENCRYPTION_ERROR);
    }
    
    /**
     * Create a FILE_DECRYPTION_ERROR exception.
     * 
     * @param message The error message
     * @param cause The cause of the error
     * @return The exception
     */
    public static FileException decryptionError(String message, Throwable cause) {
        return new FileException("File decryption error: " + message, cause, FileErrorCode.FILE_DECRYPTION_ERROR);
    }
    
    /**
     * Create a FILE_TRANSFER_ERROR exception.
     * 
     * @param message The error message
     * @param cause The cause of the error
     * @return The exception
     */
    public static FileException transferError(String message, Throwable cause) {
        return new FileException("File transfer error: " + message, cause, FileErrorCode.FILE_TRANSFER_ERROR);
    }
    
    /**
     * Create an INVALID_FILE_METADATA exception.
     * 
     * @param message The error message
     * @return The exception
     */
    public static FileException invalidMetadata(String message) {
        return new FileException("Invalid file metadata: " + message, FileErrorCode.INVALID_FILE_METADATA);
    }
    
    /**
     * Create a FILE_CHECKSUM_MISMATCH exception.
     * 
     * @param fileId The ID of the file
     * @param expectedChecksum The expected checksum
     * @param actualChecksum The actual checksum
     * @return The exception
     */
    public static FileException checksumMismatch(String fileId, String expectedChecksum, String actualChecksum) {
        return new FileException(
            "File checksum mismatch for file " + fileId + ": expected=" + expectedChecksum + ", actual=" + actualChecksum,
            FileErrorCode.FILE_CHECKSUM_MISMATCH
        );
    }
}