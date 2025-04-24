package com.p2p.model;

public class WebSocketMessage {
    
    public enum MessageType {
        JOIN,
        LEAVE,
        CHAT,
        FILE,
        ERROR
    }
    
    private MessageType type;
    private String content;
    private String sender;
    private String fileId;
    private long timestamp = System.currentTimeMillis();
    
    // Getters and Setters
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getSender() {
        return sender;
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }
    
    public String getFileId() {
        return fileId;
    }
    
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}