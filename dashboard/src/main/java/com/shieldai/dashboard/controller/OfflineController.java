package com.shieldai.dashboard.controller;

import com.shieldai.dashboard.service.OfflineDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/offline")
public class OfflineController {

    @Autowired
    private OfflineDetectionService offlineDetectionService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getOfflineStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("pendingCount", offlineDetectionService.getPendingDetectionCount());
        status.put("enabled", true);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> triggerSync() {
        int syncedCount = offlineDetectionService.syncOfflineDetections();
        int retriedCount = offlineDetectionService.retryFailedDetections(3);
        
        Map<String, Object> result = new HashMap<>();
        result.put("syncedCount", syncedCount);
        result.put("retriedCount", retriedCount);
        result.put("message", "Manual sync completed");
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importFromFileSystem() {
        int importedCount = offlineDetectionService.importFromFileSystem();
        
        Map<String, Object> result = new HashMap<>();
        result.put("importedCount", importedCount);
        result.put("message", "File system import completed");
        
        return ResponseEntity.ok(result);
    }
}