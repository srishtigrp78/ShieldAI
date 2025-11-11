package com.shieldai.dashboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.shieldai.dashboard.service.DetectionService;
import com.shieldai.shared.DetectionReport;

import jakarta.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000") // allow CRA dev server
@RestController
@RequestMapping("/api/detections")
public class DetectionController {

private final DetectionService detectionService;

public DetectionController(DetectionService detectionService) {
this.detectionService = detectionService;
}

@PostMapping
@Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
public ResponseEntity<String> receiveDetection(@Valid @RequestBody DetectionReport report) {
try {
    detectionService.saveDetection(report);
    return ResponseEntity.ok("Detection logged successfully");
} catch (Exception e) {
    System.err.println("Failed to save detection: " + e.getMessage());
    throw e; // Trigger retry
}
}

@GetMapping
public ResponseEntity<List<DetectionReport>> getAllDetections(
        @RequestParam(required = false) String candidateId,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        @RequestParam(required = false) String toolName,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "100") int size
) {
    // Limit size to prevent performance issues
    int limitedSize = Math.min(size, 500);
    
    List<DetectionReport> detections = detectionService.getFilteredDetections(
        candidateId, startDate, endDate, toolName, search, page, limitedSize);
    
    return ResponseEntity.ok(detections);
}

@GetMapping("/tools")
public ResponseEntity<List<String>> getUniqueToolNames() {
    List<String> toolNames = detectionService.getUniqueToolNames();
    return ResponseEntity.ok(toolNames);
}
}
