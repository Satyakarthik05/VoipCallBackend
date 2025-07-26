package com.example.VoipCall.controller;

import com.example.VoipCall.SignalingHandler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/calls")
public class CallController {
    private static final Logger logger = LoggerFactory.getLogger(CallController.class);

    @PostMapping("/start")
    public ResponseEntity<?> startCall(@RequestBody Map<String, Object> body) {
        String callerId = body.get("callerId").toString();
        String receiverId = body.get("receiverId").toString();
        String callerName = body.getOrDefault("callerName", "Unknown").toString();

        logger.info("Call initiated from {} to {}", callerId, receiverId);

        WebSocketSession receiverSession = SignalingHandler.getUsers().get(receiverId);
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
}