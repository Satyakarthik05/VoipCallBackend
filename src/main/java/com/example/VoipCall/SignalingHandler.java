package com.example.VoipCall;

import com.example.VoipCall.model.CallRecord;
import com.example.VoipCall.repository.CallRecordRepository;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SignalingHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(SignalingHandler.class);
    private static final Map<String, WebSocketSession> users = new ConcurrentHashMap<>();
    private final CallRecordRepository callRecordRepository;

    @Autowired
    public SignalingHandler(CallRecordRepository callRecordRepository) {
        this.callRecordRepository = callRecordRepository;
    }

    public static Map<String, WebSocketSession> getUsers() {
        return users;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("New WebSocket connection: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JSONObject json = new JSONObject(message.getPayload());
        String type = json.getString("type");
        String userId = json.optString("userId");

        switch (type) {
            case "join":
                users.put(userId, session);
                logger.info("User {} joined (Total: {})", userId, users.size());
                break;

            case "incoming_call":
                handleIncomingCall(json);
                break;

            case "call_accepted":
                handleCallAccepted(json);
                break;

            case "call_rejected":
                handleCallRejected(json);
                break;

            case "end_call":
                handleCallEnded(json);
                break;

            case "offer":
            case "answer":
            case "candidate":
                forwardMessage(json);
                break;

            default:
                logger.warn("Unknown message type: {}", type);
        }
    }

    private void forwardMessage(JSONObject json) throws Exception {
        String targetId = json.getString("target");
        WebSocketSession targetSession = users.get(targetId);

        if (targetSession != null && targetSession.isOpen()) {
            targetSession.sendMessage(new TextMessage(json.toString()));
            logger.info("Forwarded message to {}", targetId);
        } else {
            logger.error("Target {} not found or disconnected", targetId);
        }
    }

    private void handleIncomingCall(JSONObject json) throws Exception {
        String callerId = json.getString("from");
        String receiverId = json.getString("to");
        String callerName = json.optString("callerName", "Unknown");

        // Save call start time
        CallRecord call = new CallRecord();
        call.setCallerId(callerId);
        call.setReceiverId(receiverId);
        call.setStartTime(LocalDateTime.now());
        callRecordRepository.save(call);

        WebSocketSession receiverSession = users.get(receiverId);
        if (receiverSession != null && receiverSession.isOpen()) {
            JSONObject notification = new JSONObject();
            notification.put("type", "incoming_call");
            notification.put("from", callerId);
            notification.put("callerName", callerName);

            receiverSession.sendMessage(new TextMessage(notification.toString()));
            logger.info("Incoming call notification sent to {}", receiverId);
        }
    }

    private void handleCallAccepted(JSONObject json) throws Exception {
        String acceptorId = json.getString("from");
        String callerId = json.getString("to");

        WebSocketSession callerSession = users.get(callerId);
        if (callerSession != null && callerSession.isOpen()) {
            JSONObject response = new JSONObject();
            response.put("type", "call_accepted");
            response.put("from", acceptorId);
            callerSession.sendMessage(new TextMessage(response.toString()));
            logger.info("Call accepted notification sent to {}", callerId);
        }
    }

    private void handleCallRejected(JSONObject json) throws Exception {
        String rejectorId = json.getString("from");
        String callerId = json.getString("to");

        WebSocketSession callerSession = users.get(callerId);
        if (callerSession != null && callerSession.isOpen()) {
            JSONObject response = new JSONObject();
            response.put("type", "call_rejected");
            response.put("from", rejectorId);
            callerSession.sendMessage(new TextMessage(response.toString()));
            logger.info("Call rejected notification sent to {}", callerId);
        }

        // Optional: Mark call as ended if rejected
        callRecordRepository.findTopByCallerIdAndReceiverIdOrderByStartTimeDesc(callerId, rejectorId)
                .ifPresent(record -> {
                    record.setEndTime(LocalDateTime.now());
                    callRecordRepository.save(record);
                });
    }

    private void handleCallEnded(JSONObject json) {
        String callerId = json.getString("from");
        String receiverId = json.getString("to");

        callRecordRepository.findTopByCallerIdAndReceiverIdOrderByStartTimeDesc(callerId, receiverId)
                .ifPresent(record -> {
                    record.setEndTime(LocalDateTime.now());
                    callRecordRepository.save(record);
                    logger.info("Call ended: {} → {}", callerId, receiverId);
                });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        users.entrySet().removeIf(entry -> entry.getValue().equals(session));
        logger.info("WebSocket disconnected: {}", session.getId());
    }
}