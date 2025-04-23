package com.p2pexchange.exception;

/**
 * Exception for room-related errors.
 */
public class RoomException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final RoomErrorCode errorCode;
    
    /**
     * Error codes for room operations.
     */
    public enum RoomErrorCode {
        ROOM_NOT_FOUND,
        ACCESS_DENIED,
        ROOM_CREATION_ERROR,
        MEMBER_ALREADY_EXISTS,
        MEMBER_NOT_FOUND,
        FILE_ALREADY_SHARED,
        FILE_NOT_FOUND_IN_ROOM,
        MAX_MEMBERS_EXCEEDED,
        MAX_FILES_EXCEEDED,
        INVALID_ROOM_SETTINGS,
        ROOM_DELETION_ERROR
    }
    
    /**
     * Constructor with message and error code.
     * 
     * @param message The error message
     * @param errorCode The error code
     */
    public RoomException(String message, RoomErrorCode errorCode) {
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
    public RoomException(String message, Throwable cause, RoomErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Get the error code.
     * 
     * @return The error code
     */
    public RoomErrorCode getErrorCode() {
        return errorCode;
    }
    
    /**
     * Create a ROOM_NOT_FOUND exception.
     * 
     * @param roomId The ID of the room that was not found
     * @return The exception
     */
    public static RoomException roomNotFound(String roomId) {
        return new RoomException("Room not found with ID: " + roomId, RoomErrorCode.ROOM_NOT_FOUND);
    }
    
    /**
     * Create an ACCESS_DENIED exception.
     * 
     * @param userId The ID of the user who was denied access
     * @param roomId The ID of the room that was accessed
     * @return The exception
     */
    public static RoomException accessDenied(String userId, String roomId) {
        return new RoomException("Access denied for user " + userId + " to room " + roomId, RoomErrorCode.ACCESS_DENIED);
    }
    
    /**
     * Create a ROOM_CREATION_ERROR exception.
     * 
     * @param message The error message
     * @param cause The cause of the error
     * @return The exception
     */
    public static RoomException creationError(String message, Throwable cause) {
        return new RoomException("Room creation error: " + message, cause, RoomErrorCode.ROOM_CREATION_ERROR);
    }
    
    /**
     * Create a MEMBER_ALREADY_EXISTS exception.
     * 
     * @param userId The ID of the user who is already a member
     * @param roomId The ID of the room
     * @return The exception
     */
    public static RoomException memberAlreadyExists(String userId, String roomId) {
        return new RoomException("User " + userId + " is already a member of room " + roomId, RoomErrorCode.MEMBER_ALREADY_EXISTS);
    }
    
    /**
     * Create a MEMBER_NOT_FOUND exception.
     * 
     * @param userId The ID of the user who was not found
     * @param roomId The ID of the room
     * @return The exception
     */
    public static RoomException memberNotFound(String userId, String roomId) {
        return new RoomException("User " + userId + " is not a member of room " + roomId, RoomErrorCode.MEMBER_NOT_FOUND);
    }
    
    /**
     * Create a FILE_ALREADY_SHARED exception.
     * 
     * @param fileId The ID of the file that is already shared
     * @param roomId The ID of the room
     * @return The exception
     */
    public static RoomException fileAlreadyShared(String fileId, String roomId) {
        return new RoomException("File " + fileId + " is already shared in room " + roomId, RoomErrorCode.FILE_ALREADY_SHARED);
    }
    
    /**
     * Create a FILE_NOT_FOUND_IN_ROOM exception.
     * 
     * @param fileId The ID of the file that was not found
     * @param roomId The ID of the room
     * @return The exception
     */
    public static RoomException fileNotFoundInRoom(String fileId, String roomId) {
        return new RoomException("File " + fileId + " not found in room " + roomId, RoomErrorCode.FILE_NOT_FOUND_IN_ROOM);
    }
    
    /**
     * Create a MAX_MEMBERS_EXCEEDED exception.
     * 
     * @param roomId The ID of the room
     * @param maxMembers The maximum number of members allowed
     * @return The exception
     */
    public static RoomException maxMembersExceeded(String roomId, int maxMembers) {
        return new RoomException("Maximum number of members (" + maxMembers + ") exceeded for room " + roomId, RoomErrorCode.MAX_MEMBERS_EXCEEDED);
    }
    
    /**
     * Create a MAX_FILES_EXCEEDED exception.
     * 
     * @param roomId The ID of the room
     * @param maxFiles The maximum number of files allowed
     * @return The exception
     */
    public static RoomException maxFilesExceeded(String roomId, int maxFiles) {
        return new RoomException("Maximum number of files (" + maxFiles + ") exceeded for room " + roomId, RoomErrorCode.MAX_FILES_EXCEEDED);
    }
    
    /**
     * Create an INVALID_ROOM_SETTINGS exception.
     * 
     * @param message The error message
     * @return The exception
     */
    public static RoomException invalidRoomSettings(String message) {
        return new RoomException("Invalid room settings: " + message, RoomErrorCode.INVALID_ROOM_SETTINGS);
    }
    
    /**
     * Create a ROOM_DELETION_ERROR exception.
     * 
     * @param roomId The ID of the room
     * @param cause The cause of the error
     * @return The exception
     */
    public static RoomException deletionError(String roomId, Throwable cause) {
        return new RoomException("Error deleting room " + roomId, cause, RoomErrorCode.ROOM_DELETION_ERROR);
    }
}