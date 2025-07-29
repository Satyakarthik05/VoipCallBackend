package com.example.VoipCall.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public class CallRecordingDTO {
    private String callerId;
    private String receiverId;
   private Instant startTime;  // Changed from String
    private Instant endTime; 
    private String duration;
    private String recording;  // Base64 encoded recording
    private String fileName;

    // Getters and Setters
    public String getCallerId() { return callerId; }
    public void setCallerId(String callerId) { this.callerId = callerId; }
    
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    
 public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    
    public String getRecording() { return recording; }
    public void setRecording(String recording) { this.recording = recording; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}