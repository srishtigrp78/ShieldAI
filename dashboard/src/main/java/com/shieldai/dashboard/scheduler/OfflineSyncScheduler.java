package com.shieldai.dashboard.scheduler;

import com.shieldai.dashboard.service.OfflineDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "shieldai.offline.storage.enabled", havingValue = "true", matchIfMissing = true)
public class OfflineSyncScheduler {

    @Autowired
    private OfflineDetectionService offlineDetectionService;

    @Value("${shieldai.offline.sync.max-retries:3}")
    private int maxRetries;

    /**
     * Sync offline detections every 30 seconds
     */
    @Scheduled(fixedDelayString = "${shieldai.offline.sync.interval:30000}")
    public void syncOfflineDetections() {
        try {
            long pendingCount = offlineDetectionService.getPendingDetectionCount();
            if (pendingCount > 0) {
                System.out.println("🔄 Starting offline sync - " + pendingCount + " pending detections");
                
                int syncedCount = offlineDetectionService.syncOfflineDetections();
                if (syncedCount > 0) {
                    System.out.println("✅ Synced " + syncedCount + " offline detections");
                }
                
                // Retry failed detections
                int retriedCount = offlineDetectionService.retryFailedDetections(maxRetries);
                if (retriedCount > 0) {
                    System.out.println("🔄 Retried " + retriedCount + " failed detections");
                }
            }
        } catch (Exception e) {
            System.err.println("Offline sync failed: " + e.getMessage());
        }
    }

    /**
     * Import from file system every 5 minutes
     */
    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void importFromFileSystem() {
        try {
            int importedCount = offlineDetectionService.importFromFileSystem();
            if (importedCount > 0) {
                System.out.println("📁 Imported " + importedCount + " detections from file system");
            }
        } catch (Exception e) {
            System.err.println("File system import failed: " + e.getMessage());
        }
    }

    /**
     * Cleanup old synced records daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldRecords() {
        try {
            offlineDetectionService.cleanupOldSyncedRecords(7); // Keep 7 days
            System.out.println("🧹 Cleaned up old synced records");
        } catch (Exception e) {
            System.err.println("Cleanup failed: " + e.getMessage());
        }
    }
}