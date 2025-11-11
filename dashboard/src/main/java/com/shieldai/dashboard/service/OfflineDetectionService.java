package com.shieldai.dashboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shieldai.dashboard.entity.OfflineDetectionSyncEntity;
import com.shieldai.dashboard.repository.OfflineDetectionSyncRepository;
import com.shieldai.shared.DetectionReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OfflineDetectionService {

    @Autowired
    private OfflineDetectionSyncRepository syncRepository;

    @Autowired
    @Lazy
    private DetectionService detectionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${shieldai.offline.storage.enabled:true}")
    private boolean offlineStorageEnabled;

    @Value("${shieldai.offline.storage.directory:${user.home}/.shieldai/offline}")
    private String offlineStorageDirectory;

    /**
     * Store detection offline when main database is unreachable
     */
    @Transactional
    public void storeDetectionOffline(DetectionReport report) {
        if (!offlineStorageEnabled) {
            return;
        }

        try {
            String jsonData = objectMapper.writeValueAsString(report);
            OfflineDetectionSyncEntity syncEntity = new OfflineDetectionSyncEntity(jsonData);
            syncRepository.save(syncEntity);
            
            // Also store in file system as backup
            storeInFileSystem(report);
            
            System.out.println("💾 Detection stored offline: " + report.getCandidateId());
        } catch (Exception e) {
            System.err.println("Failed to store detection offline: " + e.getMessage());
            // Fallback to file system only
            storeInFileSystem(report);
        }
    }

    /**
     * Sync all pending offline detections to main database
     */
    @Transactional
    public int syncOfflineDetections() {
        List<OfflineDetectionSyncEntity> pendingDetections = 
                syncRepository.findBySyncStatusOrderByCreatedAtAsc(
                        OfflineDetectionSyncEntity.SyncStatus.PENDING);

        int syncedCount = 0;
        for (OfflineDetectionSyncEntity syncEntity : pendingDetections) {
            try {
                DetectionReport report = objectMapper.readValue(
                        syncEntity.getDetectionData(), DetectionReport.class);
                
                detectionService.saveDetection(report);
                syncEntity.markAsSynced();
                syncRepository.save(syncEntity);
                syncedCount++;
                
                System.out.println("🔄 Synced offline detection: " + report.getCandidateId());
            } catch (Exception e) {
                syncEntity.markAsFailed(e.getMessage());
                syncRepository.save(syncEntity);
                System.err.println("Failed to sync detection: " + e.getMessage());
            }
        }

        return syncedCount;
    }

    /**
     * Retry failed detections
     */
    @Transactional
    public int retryFailedDetections(int maxRetries) {
        List<OfflineDetectionSyncEntity> failedDetections = 
                syncRepository.findFailedDetectionsForRetry(maxRetries);

        int retriedCount = 0;
        for (OfflineDetectionSyncEntity syncEntity : failedDetections) {
            try {
                DetectionReport report = objectMapper.readValue(
                        syncEntity.getDetectionData(), DetectionReport.class);
                
                detectionService.saveDetection(report);
                syncEntity.markAsSynced();
                syncRepository.save(syncEntity);
                retriedCount++;
                
                System.out.println("🔄 Retried offline detection: " + report.getCandidateId());
            } catch (Exception e) {
                syncEntity.markAsFailed(e.getMessage());
                syncRepository.save(syncEntity);
                System.err.println("Retry failed for detection: " + e.getMessage());
            }
        }

        return retriedCount;
    }

    /**
     * Get count of pending offline detections
     */
    public long getPendingDetectionCount() {
        return syncRepository.countBySyncStatus(OfflineDetectionSyncEntity.SyncStatus.PENDING);
    }

    /**
     * Clean up old synced records
     */
    @Transactional
    public void cleanupOldSyncedRecords(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        syncRepository.deleteBySyncStatusAndSyncedAtBefore(
                OfflineDetectionSyncEntity.SyncStatus.SYNCED, cutoffDate);
    }

    /**
     * Import detections from file system to database
     */
    @Transactional
    public int importFromFileSystem() {
        Path offlineDir = Paths.get(offlineStorageDirectory);
        if (!Files.exists(offlineDir)) {
            return 0;
        }

        int importedCount = 0;
        try {
            Files.list(offlineDir)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            String content = Files.readString(path);
                            DetectionReport report = objectMapper.readValue(content, DetectionReport.class);
                            storeDetectionOffline(report);
                            Files.delete(path);
                        } catch (Exception e) {
                            System.err.println("Failed to import file: " + path + " - " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Failed to import from file system: " + e.getMessage());
        }

        return importedCount;
    }

    private void storeInFileSystem(DetectionReport report) {
        try {
            Path offlineDir = Paths.get(offlineStorageDirectory);
            Files.createDirectories(offlineDir);

            String filename = String.format("detection_%s_%d.json", 
                    report.getCandidateId(), System.currentTimeMillis());
            Path filePath = offlineDir.resolve(filename);

            String json = objectMapper.writeValueAsString(report);
            Files.write(filePath, json.getBytes());
        } catch (Exception e) {
            System.err.println("Failed to store in file system: " + e.getMessage());
        }
    }
}