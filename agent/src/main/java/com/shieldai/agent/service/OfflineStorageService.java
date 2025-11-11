package com.shieldai.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shieldai.shared.DetectionReport;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Offline storage service for failed detections
 */
public class OfflineStorageService {
    
    private final String storageDir;
    private final ObjectMapper objectMapper;
    
    public OfflineStorageService() {
        this.storageDir = System.getProperty("user.home") + "/.shieldai/offline";
        this.objectMapper = new ObjectMapper();
        createStorageDirectory();
    }
    
    private void createStorageDirectory() {
        try {
            Files.createDirectories(Paths.get(storageDir));
        } catch (IOException e) {
            System.err.println("Failed to create offline storage directory: " + e.getMessage());
        }
    }
    
    /**
     * Store detection offline when backend is unreachable
     */
    public void storeOffline(DetectionReport report) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS"));
            String filename = "detection_" + timestamp + ".json";
            Path filePath = Paths.get(storageDir, filename);
            
            String json = objectMapper.writeValueAsString(report);
            Files.write(filePath, json.getBytes());
            
            System.out.println("💾 Detection stored offline: " + filename);
        } catch (IOException e) {
            System.err.println("Failed to store detection offline: " + e.getMessage());
        }
    }
    
    /**
     * Get all offline stored detections
     */
    public List<DetectionReport> getOfflineDetections() {
        List<DetectionReport> detections = new ArrayList<>();
        
        try {
            File dir = new File(storageDir);
            File[] files = dir.listFiles((d, name) -> name.startsWith("detection_") && name.endsWith(".json"));
            
            if (files != null) {
                for (File file : files) {
                    try {
                        String json = Files.readString(file.toPath());
                        DetectionReport report = objectMapper.readValue(json, DetectionReport.class);
                        detections.add(report);
                    } catch (IOException e) {
                        System.err.println("Failed to read offline detection: " + file.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load offline detections: " + e.getMessage());
        }
        
        return detections;
    }
    
    /**
     * Sync offline detections to backend
     */
    public CompletableFuture<Integer> syncOfflineDetections(com.shieldai.shared.ApiClient apiClient) {
        return CompletableFuture.supplyAsync(() -> {
            List<DetectionReport> offlineDetections = getOfflineDetections();
            int syncedCount = 0;
            
            for (DetectionReport detection : offlineDetections) {
                try {
                    boolean success = apiClient.sendDetectionAsync(detection).get();
                    if (success) {
                        deleteOfflineDetection(detection);
                        syncedCount++;
                    }
                } catch (Exception e) {
                    System.err.println("Failed to sync detection: " + e.getMessage());
                }
            }
            
            if (syncedCount > 0) {
                System.out.println("🔄 Synced " + syncedCount + " offline detections");
            }
            
            return syncedCount;
        });
    }
    
    private void deleteOfflineDetection(DetectionReport report) {
        try {
            File dir = new File(storageDir);
            File[] files = dir.listFiles((d, name) -> name.startsWith("detection_") && name.endsWith(".json"));
            
            if (files != null) {
                for (File file : files) {
                    try {
                        String json = Files.readString(file.toPath());
                        DetectionReport stored = objectMapper.readValue(json, DetectionReport.class);
                        
                        if (stored.getCandidateId().equals(report.getCandidateId()) && 
                            stored.getTimestamp().equals(report.getTimestamp())) {
                            file.delete();
                            break;
                        }
                    } catch (IOException e) {
                        // Continue to next file
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to delete synced detection: " + e.getMessage());
        }
    }
    
    public int getOfflineCount() {
        return getOfflineDetections().size();
    }
}