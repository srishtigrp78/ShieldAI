package com.shieldai.dashboard.controller;

import com.shieldai.dashboard.service.DetectionService;
import com.shieldai.shared.DetectionReport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/agent")
@CrossOrigin(origins = "http://localhost:3000")
public class AgentController {

    private final DetectionService detectionService;

    public AgentController(DetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @PostMapping("/detections")
    public ResponseEntity<String> receiveDetection(@Valid @RequestBody DetectionReport report) {
        detectionService.saveDetection(report);
        return ResponseEntity.ok("Detection logged successfully");
    }
}