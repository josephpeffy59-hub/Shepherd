package com.crowdguard.controller;

import com.crowdguard.model.Alert;
import com.crowdguard.model.User;
import com.crowdguard.repository.UserRepository;
import com.crowdguard.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/alert")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final UserRepository userRepo;

    @PostMapping("/trigger")
    public ResponseEntity<?> trigger(@RequestBody Map<String, Double> body, Authentication auth) {
        User u = userRepo.findByEmail(auth.getName()).orElseThrow();
        Alert a = alertService.trigger(u, body.get("latitude"), body.get("longitude"));
        return ResponseEntity.ok(Map.of("alertId", a.getId().toString()));
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancel(Authentication auth) {
        User u = userRepo.findByEmail(auth.getName()).orElseThrow();
        alertService.cancel(u);
        return ResponseEntity.ok().build();
    }
}