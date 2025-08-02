package com.example.VoipCall.controller;

import com.example.VoipCall.model.User;
import com.example.VoipCall.model.CallRecord;
import com.example.VoipCall.service.AdminService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Get all doctors
    @GetMapping("/doctors")
    public ResponseEntity<List<User>> getAllDoctors() {
        return ResponseEntity.ok(adminService.getAllDoctors());
    }

    // Get all patients
    @GetMapping("/patients")
    public ResponseEntity<List<User>> getAllPatients() {
        return ResponseEntity.ok(adminService.getAllPatients());
    }

    // Get all call records
    @GetMapping("/call-records")
    public ResponseEntity<List<CallRecord>> getAllCallRecords() {
        return ResponseEntity.ok(adminService.getAllCallRecords());
    }

    // Get call records by doctor ID
    @GetMapping("/call-records/doctor/{doctorId}")
    public ResponseEntity<List<CallRecord>> getCallRecordsByDoctor(@PathVariable String doctorId) {
        return ResponseEntity.ok(adminService.getCallRecordsByDoctor(doctorId));
    }

    // Get call records by patient ID
    @GetMapping("/call-records/patient/{patientId}")
    public ResponseEntity<List<CallRecord>> getCallRecordsByPatient(@PathVariable String patientId) {
        return ResponseEntity.ok(adminService.getCallRecordsByPatient(patientId));
    }

    // Get call recording file
    @GetMapping("/recordings/{recordingId}")
    public ResponseEntity<Resource> getRecording(@PathVariable Long recordingId) {
        Resource recording = adminService.getRecordingFile(recordingId);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + recording.getFilename() + "\"")
                .body(recording);
    }

    // Update user details
    @PutMapping("/users/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Long userId, @RequestBody User userDetails) {
        return ResponseEntity.ok(adminService.updateUser(userId, userDetails));
    }

    // Delete user
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok().build();
 }
 @PostMapping("/transcribe/{callId}")
public ResponseEntity<String> transcribeCall(@PathVariable Long callId) {
    String transcription = adminService.transcribeCallRecording(callId);
    return ResponseEntity.ok(transcription);
}

}
