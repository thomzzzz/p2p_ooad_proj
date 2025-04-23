package com.p2pexchange.model;

import java.util.Date;

/**
 * Model for tracking file transfer progress.
 */
public class TransferProgress {
    private String fileId;
    private long totalSize;
    private long bytesTransferred;
    private Date startTime;
    private Date lastUpdateTime;
    private TransferState state;
    
    /**
     * Enum for transfer states.
     */
    public enum TransferState {
        INITIALIZED,
        IN_PROGRESS,
        PAUSED,
        COMPLETED,
        FAILED,
        CANCELLED
    }
    
    /**
     * Default constructor.
     */
    public TransferProgress() {
        this.startTime = new Date();
        this.lastUpdateTime = new Date();
        this.state = TransferState.INITIALIZED;
        this.bytesTransferred = 0;
    }
    
    /**
     * Constructor with file ID and total size.
     * 
     * @param fileId The file ID
     * @param totalSize The total file size in bytes
     */
    public TransferProgress(String fileId, long totalSize) {
        this.fileId = fileId;
        this.totalSize = totalSize;
        this.bytesTransferred = 0;
        this.startTime = new Date();
        this.lastUpdateTime = new Date();
        this.state = TransferState.INITIALIZED;
    }
    
    /**
     * Update the progress with additional transferred bytes.
     * 
     * @param newBytes The number of newly transferred bytes
     */
    public void update(long newBytes) {
        this.bytesTransferred += newBytes;
        this.lastUpdateTime = new Date();
        
        if (this.state == TransferState.INITIALIZED) {
            this.state = TransferState.IN_PROGRESS;
        }
        
        if (this.bytesTransferred >= this.totalSize) {
            this.state = TransferState.COMPLETED;
        }
    }
    
    /**
     * Get the completion percentage.
     * 
     * @return The completion percentage (0-100)
     */
    public double getCompletionPercentage() {
        if (totalSize == 0) {
            return 0;
        }
        return (double) bytesTransferred / totalSize * 100;
    }
    
    /**
     * Get the transfer rate in bytes per second.
     * 
     * @return The transfer rate
     */
    public long getTransferRate() {
        long elapsedMillis = lastUpdateTime.getTime() - startTime.getTime();
        if (elapsedMillis == 0) {
            return 0;
        }
        return bytesTransferred * 1000 / elapsedMillis; // bytes per second
    }
    
    /**
     * Get the estimated time remaining in seconds.
     * 
     * @return The estimated time remaining
     */
    public long getEstimatedTimeRemaining() {
        long rate = getTransferRate();
        if (rate == 0) {
            return -1; // Cannot estimate
        }
        
        long remainingBytes = totalSize - bytesTransferred;
        return remainingBytes / rate; // seconds
    }
    
    /**
     * Pause the transfer.
     */
    public void pause() {
        if (this.state == TransferState.IN_PROGRESS) {
            this.state = TransferState.PAUSED;
        }
    }
    
    /**
     * Resume the transfer.
     */
    public void resume() {
        if (this.state == TransferState.PAUSED) {
            this.state = TransferState.IN_PROGRESS;
            this.lastUpdateTime = new Date();
        }
    }
    
    /**
     * Cancel the transfer.
     */
    public void cancel() {
        this.state = TransferState.CANCELLED;
    }
    
    /**
     * Mark the transfer as failed.
     */
    public void fail() {
        this.state = TransferState.FAILED;
    }
    
    /**
     * Check if the transfer is active.
     * 
     * @return True if the transfer is active, false otherwise
     */
    public boolean isActive() {
        return this.state == TransferState.IN_PROGRESS || this.state == TransferState.INITIALIZED;
    }
    
    /**
     * Check if the transfer is completed.
     * 
     * @return True if the transfer is completed, false otherwise
     */
    public boolean isCompleted() {
        return this.state == TransferState.COMPLETED;
    }
    
    /**
     * Get the elapsed time in milliseconds.
     * 
     * @return The elapsed time
     */
    public long getElapsedTime() {
        return lastUpdateTime.getTime() - startTime.getTime();
    }
    
    // Getters and Setters
    
    public String getFileId() {
        return fileId;
    }
    
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
    
    public long getTotalSize() {
        return totalSize;
    }
    
    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }
    
    public long getBytesTransferred() {
        return bytesTransferred;
    }
    
    public void setBytesTransferred(long bytesTransferred) {
        this.bytesTransferred = bytesTransferred;
    }
    
    public Date getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
    
    public TransferState getState() {
        return state;
    }
    
    public void setState(TransferState state) {
        this.state = state;
    }
}