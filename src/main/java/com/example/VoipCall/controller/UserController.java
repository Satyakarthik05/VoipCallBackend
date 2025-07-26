package com.example.VoipCall.controller;

import com.example.VoipCall.model.User;
import com.example.VoipCall.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/role-view")
    public ResponseEntity<List<User>> getUsersBasedOnRole(@RequestParam String role) {
        if ("DOCTOR".equalsIgnoreCase(role)) {
            return ResponseEntity.ok(userService.getDoctors());
        } else if ("PATIENT".equalsIgnoreCase(role)) {
            return ResponseEntity.ok(userService.getPatients());
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}