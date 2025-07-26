package com.example.VoipCall.repository;

import com.example.VoipCall.model.CallRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CallRecordRepository extends JpaRepository<CallRecord, Long> {
    Optional<CallRecord> findTopByCallerIdAndReceiverIdOrderByStartTimeDesc(String callerId, String receiverId);
}