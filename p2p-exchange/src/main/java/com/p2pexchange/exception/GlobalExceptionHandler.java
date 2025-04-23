package com.p2pexchange.exception;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Global exception handler for the application.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Error response model.
     */
    public static class ErrorResponse {
        private final Date timestamp;
        private final int status;
        private final String error;
        private final String message;
        private final String path;
        private Object details;

        public ErrorResponse(Date timestamp, int status, String error, String message, String path) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public int getStatus() {
            return status;
        }

        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }

        public String getPath() {
            return path;
        }

        public Object getDetails() {
            return details;
        }

        public void setDetails(Object details) {
            this.details = details;
        }
    }

    /**
     * Handle FileException.
     * 
     * @param ex The exception to handle
     * @param request The current request
     * @return An appropriate response entity
     */
    @ExceptionHandler(FileException.class)
    public ResponseEntity<ErrorResponse> handleFileException(FileException ex, HttpServletRequest request) {
        HttpStatus status;
        
        // Map error codes to HTTP status codes
        switch (ex.getErrorCode()) {
            case FILE_NOT_FOUND:
                status = HttpStatus.NOT_FOUND;
                break;
            case FILE_ACCESS_DENIED:
                status = HttpStatus.FORBIDDEN;
                break;
            case FILE_SIZE_EXCEEDED:
            case FILE_TYPE_NOT_SUPPORTED:
            case INVALID_FILE_METADATA:
                status = HttpStatus.BAD_REQUEST;
                break;
            default:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            status.value(),
            ex.getErrorCode().toString(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Handle RoomException.
     * 
     * @param ex The exception to handle
     * @param request The current request
     * @return An appropriate response entity
     */
    @ExceptionHandler(RoomException.class)
    public ResponseEntity<ErrorResponse> handleRoomException(RoomException ex, HttpServletRequest request) {
        HttpStatus status;
        
        // Map error codes to HTTP status codes
        switch (ex.getErrorCode()) {
            case ROOM_NOT_FOUND:
            case FILE_NOT_FOUND_IN_ROOM:
            case MEMBER_NOT_FOUND:
                status = HttpStatus.NOT_FOUND;
                break;
            case ACCESS_DENIED:
                status = HttpStatus.FORBIDDEN;
                break;
            case MEMBER_ALREADY_EXISTS:
            case FILE_ALREADY_SHARED:
            case MAX_MEMBERS_EXCEEDED:
            case MAX_FILES_EXCEEDED:
            case INVALID_ROOM_SETTINGS:
                status = HttpStatus.BAD_REQUEST;
                break;
            default:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            status.value(),
            ex.getErrorCode().toString(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Handle AccessDeniedException.
     * 
     * @param ex The exception to handle
     * @param request The current request
     * @return An appropriate response entity
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.FORBIDDEN.value(),
            "ACCESS_DENIED",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle BadCredentialsException.
     * 
     * @param ex The exception to handle
     * @param request The current request
     * @return An appropriate response entity
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.UNAUTHORIZED.value(),
            "INVALID_CREDENTIALS",
            "Invalid username or password",
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle MaxUploadSizeExceededException.
     * 
     * @param ex The exception to handle
     * @param request The current request
     * @return An appropriate response entity
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.BAD_REQUEST.value(),
            "MAX_UPLOAD_SIZE_EXCEEDED",
            "File size exceeds the maximum allowed size",
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle MethodArgumentNotValidException.
     * 
     * @param ex The exception to handle
     * @param request The current request
     * @return An appropriate response entity
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_ERROR",
            "Validation failed for one or more fields",
            request.getRequestURI()
        );
        errorResponse.setDetails(errors);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle MissingServletRequestParameterException.
     * 
     * @param ex The exception to handle
     * @param request The current request
     * @return An appropriate response entity
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.BAD_REQUEST.value(),
            "MISSING_PARAMETER",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle all other exceptions.
     * 
     * @param ex The exception to handle
     * @param request The current request
     * @return An appropriate response entity
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "INTERNAL_SERVER_ERROR",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}