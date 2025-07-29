package com.example.VoipCall.controller;

import com.example.VoipCall.SignalingHandler;
import com.example.VoipCall.dto.CallRecordingDTO;
import com.example.VoipCall.model.CallRecord;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import com.example.VoipCall.repository.CallRecordRepository;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.file.Path;
import java.util.Base64;

@RestController
@RequestMapping("/api/calls")
public class CallController {
    private static final Logger logger = LoggerFactory.getLogger(CallController.class);
    
    private final CallRecordRepository callRecordRepository;
    private final SignalingHandler signalingHandler;

    public CallController(CallRecordRepository callRecordRepository, SignalingHandler signalingHandler) {
        this.callRecordRepository = callRecordRepository;
        this.signalingHandler = signalingHandler;
    }

    @PostMapping("/start")
    public ResponseEntity<?> startCall(@RequestBody Map<String, Object> body) {
        String callerId = body.get("callerId").toString();
        String receiverId = body.get("receiverId").toString();
        String callerName = body.getOrDefault("callerName", "Unknown").toString();

        logger.info("Call initiated from {} to {}", callerId, receiverId);

        WebSocketSession receiverSession = signalingHandler.getUsers().get(receiverId);
        if (receiverSession != null && receiverSession.isOpen()) {
            try {
                JSONObject notification = new JSONObject();
                notification.put("type", "incoming_call");
                notification.put("from", callerId);
                notification.put("callerName", callerName);

                receiverSession.sendMessage(new TextMessage(notification.toString()));
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                logger.error("Failed to send notification", e);
                return ResponseEntity.badRequest().body("Failed to notify receiver");
            }
        } else {
            return ResponseEntity.badRequest().body("Receiver not available");
        }
    }
@PostMapping("/save")
public ResponseEntity<?> saveCallRecording(@RequestBody CallRecordingDTO recordingDTO) {
    try {
        CallRecord callRecord = new CallRecord();
        callRecord.setCallerId(recordingDTO.getCallerId());
        callRecord.setReceiverId(recordingDTO.getReceiverId());
        
        // These will now parse automatically if the input is in ISO-8601 format
        callRecord.setStartTime(recordingDTO.getStartTime().atZone(ZoneId.systemDefault()).toLocalDateTime());
        callRecord.setEndTime(recordingDTO.getEndTime().atZone(ZoneId.systemDefault()).toLocalDateTime());
        
        callRecord.setDuration(recordingDTO.getDuration());
            
            // Save video file
            String uploadDir = "call_recordings";
            Path uploadPath = Paths.get(uploadDir);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String fileName = recordingDTO.getFileName();
            Path filePath = uploadPath.resolve(fileName);
            
            byte[] videoBytes = Base64.getDecoder().decode(recordingDTO.getRecording());
            Files.write(filePath, videoBytes);
            
            callRecord.setRecordingPath(filePath.toString());
            callRecordRepository.save(callRecord);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed to save recording", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to save recording: " + e.getMessage());
        }
    }
}