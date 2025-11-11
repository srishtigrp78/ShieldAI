package com.shieldai.dashboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    @GetMapping("/risk-scores")
    public ResponseEntity<Map<String, Object>> getRiskScores() {
        Map<String, Object> response = new HashMap<>();
        
        // Mock data - in real implementation, this would come from analytics service
        Map<String, Double> riskScores = new HashMap<>();
        riskScores.put("candidate-001", 0.85);
        riskScores.put("candidate-002", 0.62);
        riskScores.put("candidate-003", 0.34);
        
        response.put("riskScores", riskScores);
        response.put("timestamp", new Date());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<Map<String, Object>>> getAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        Map<String, Object> alert1 = new HashMap<>();
        alert1.put("candidateId", "candidate-001");
        alert1.put("level", "CRITICAL");
        alert1.put("message", "Critical risk detected: Multiple AI tools detected");
        alert1.put("riskScore", 0.85);
        alert1.put("timestamp", new Date());
        alerts.add(alert1);
        
        Map<String, Object> alert2 = new HashMap<>();
        alert2.put("candidateId", "candidate-002");
        alert2.put("level", "HIGH");
        alert2.put("message", "High risk detected: Burst activity pattern");
        alert2.put("riskScore", 0.62);
        alert2.put("timestamp", new Date());
        alerts.add(alert2);
        
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/patterns/{candidateId}")
    public ResponseEntity<Map<String, Object>> getPatterns(@PathVariable String candidateId) {
        Map<String, Object> response = new HashMap<>();
        
        List<Map<String, Object>> patterns = new ArrayList<>();
        
        Map<String, Object> pattern1 = new HashMap<>();
        pattern1.put("type", "BURST_ACTIVITY");
        pattern1.put("description", "High activity burst: 8 detections in hour");
        pattern1.put("severity", 0.8);
        patterns.add(pattern1);
        
        Map<String, Object> pattern2 = new HashMap<>();
        pattern2.put("type", "TOOL_SWITCHING");
        pattern2.put("description", "Multiple tools detected: ChatGPT, Claude, Copilot");
        pattern2.put("severity", 0.7);
        patterns.add(pattern2);
        
        response.put("candidateId", candidateId);
        response.put("patterns", patterns);
        
        return ResponseEntity.ok(response);
    }
}