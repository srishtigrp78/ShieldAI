package com.shieldai.dashboard.repository;

import com.shieldai.dashboard.entity.OfflineDetectionSyncEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OfflineDetectionSyncRepository extends JpaRepository<OfflineDetectionSyncEntity, Long> {

    /**
     * Find all pending detections for sync
     */
    List<OfflineDetectionSyncEntity> findBySyncStatusOrderByCreatedAtAsc(
            OfflineDetectionSyncEntity.SyncStatus syncStatus);

    /**
     * Find failed detections that can be retried
     */
    @Query("SELECT o FROM OfflineDetectionSyncEntity o WHERE o.syncStatus = 'FAILED' AND o.retryCount < :maxRetries")
    List<OfflineDetectionSyncEntity> findFailedDetectionsForRetry(@Param("maxRetries") int maxRetries);

    /**
     * Find old synced records for cleanup
     */
    @Query("SELECT o FROM OfflineDetectionSyncEntity o WHERE o.syncStatus = 'SYNCED' AND o.syncedAt < :cutoffDate")
    List<OfflineDetectionSyncEntity> findOldSyncedRecords(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count pending detections
     */
    long countBySyncStatus(OfflineDetectionSyncEntity.SyncStatus syncStatus);

    /**
     * Delete synced records older than specified date
     */
    void deleteBySyncStatusAndSyncedAtBefore(
            OfflineDetectionSyncEntity.SyncStatus syncStatus, 
            LocalDateTime cutoffDate);
}