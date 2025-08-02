package com.example.VoipCall.service;

import com.example.VoipCall.model.User;
import com.example.VoipCall.model.CallRecord;
import com.example.VoipCall.repository.UserRepository;
import com.example.VoipCall.repository.CallRecordRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final CallRecordRepository callRecordRepository;
    private final Path recordingStorageLocation;

    public AdminService(UserRepository userRepository, 
                       CallRecordRepository callRecordRepository) {
        this.userRepository = userRepository;
        this.callRecordRepository = callRecordRepository;
        this.recordingStorageLocation = Paths.get("call_recordings").toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.recordingStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public List<User> getAllDoctors() {
        return userRepository.findByRole("DOCTOR");
    }

    public List<User> getAllPatients() {
        return userRepository.findByRole("PATIENT");
    }

    public List<CallRecord> getAllCallRecords() {
        return callRecordRepository.findAll();
    }

    public List<CallRecord> getCallRecordsByDoctor(String doctorId) {
        return callRecordRepository.findByReceiverId(doctorId);
    }

    public List<CallRecord> getCallRecordsByPatient(String patientId) {
        return callRecordRepository.findByCallerId(patientId);
    }

    public Resource getRecordingFile(Long recordingId) {
        try {
            CallRecord callRecord = callRecordRepository.findById(recordingId)
                    .orElseThrow(() -> new RuntimeException("Recording not found with id " + recordingId));
            
            Path filePath = this.recordingStorageLocation.resolve(callRecord.getRecordingPath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + callRecord.getRecordingPath());
            }
        } catch (Exception ex) {
            throw new RuntimeException("Could not retrieve recording file " + recordingId, ex);
        }
    }

    public User updateUser(Long userId, User userDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        
        if (userDetails.getUsername() != null) {
            user.setUsername(userDetails.getUsername());
        }
        if (userDetails.getPassword() != null) {
            user.setPassword(userDetails.getPassword());
        }
        if (userDetails.getFullName() != null) {
            user.setFullName(userDetails.getFullName());
        }
        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }
        
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        
        userRepository.delete(user);
    }

    public String transcribeCallRecording(Long callRecordId) {
    CallRecord callRecord = callRecordRepository.findById(callRecordId)
            .orElseThrow(() -> new RuntimeException("Recording not found with id " + callRecordId));

    Path filePath = this.recordingStorageLocation.resolve(callRecord.getRecordingPath()).normalize();
    FileSystemResource fileResource = new FileSystemResource(filePath.toFile());

    // Prepare multipart request
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("audio", fileResource);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    // Send POST request
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> response = restTemplate.postForEntity("http://192.168.29.164:8080/api/transcribe", requestEntity, String.class);

    return response.getBody(); // Return transcription result
}
}
