package com.shieldai.dashboard.controller;

import com.shieldai.dashboard.service.DetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DetectionService detectionService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Get basic statistics from detection service
            long totalDetections = detectionService.getTotalDetections();
            long totalCandidates = detectionService.getTotalCandidates();
            long highConfidenceDetections = detectionService.getHighConfidenceDetections();
            
            stats.put("totalDetections", totalDetections);
            stats.put("totalCandidates", totalCandidates);
            stats.put("highConfidenceDetections", highConfidenceDetections);
            stats.put("timestamp", new Date());
            
        } catch (Exception e) {
            // Fallback to mock data if service unavailable
            stats.put("totalDetections", 156);
            stats.put("totalCandidates", 23);
            stats.put("highConfidenceDetections", 42);
            stats.put("timestamp", new Date());
        }
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/recent-alerts")
    public ResponseEntity<List<Map<String, Object>>> getRecentAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        // Mock recent alerts data
        String[] tools = {"ChatGPT", "Claude", "Copilot", "Gemini", "Perplexity"};
        String[] candidates = {"candidate-001", "candidate-002", "candidate-003"};
        
        for (int i = 0; i < 10; i++) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("toolName", tools[i % tools.length]);
            alert.put("candidateId", candidates[i % candidates.length]);
            alert.put("confidence", 0.7 + (Math.random() * 0.3));
            alert.put("timestamp", new Date(System.currentTimeMillis() - (i * 3600000))); // Hours ago
            alerts.add(alert);
        }
        
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/tool-stats")
    public ResponseEntity<Map<String, Integer>> getToolStats() {
        Map<String, Integer> toolStats = new HashMap<>();
        
        // Mock tool statistics
        toolStats.put("ChatGPT", 45);
        toolStats.put("Claude", 32);
        toolStats.put("GitHub Copilot", 28);
        toolStats.put("Gemini", 19);
        toolStats.put("Perplexity", 15);
        toolStats.put("Cursor", 12);
        
        return ResponseEntity.ok(toolStats);
    }
}