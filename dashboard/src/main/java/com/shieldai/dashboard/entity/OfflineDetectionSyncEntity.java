package com.shieldai.dashboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "offline_detection_sync")
public class OfflineDetectionSyncEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "detection_data", columnDefinition = "jsonb", nullable = false)
    private String detectionData;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false)
    private SyncStatus syncStatus = SyncStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    public enum SyncStatus {
        PENDING, SYNCED, FAILED, RETRY
    }

    // Constructors
    public OfflineDetectionSyncEntity() {}

    public OfflineDetectionSyncEntity(String detectionData) {
        this.detectionData = detectionData;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDetectionData() { return detectionData; }
    public void setDetectionData(String detectionData) { this.detectionData = detectionData; }

    public SyncStatus getSyncStatus() { return syncStatus; }
    public void setSyncStatus(SyncStatus syncStatus) { this.syncStatus = syncStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getSyncedAt() { return syncedAt; }
    public void setSyncedAt(LocalDateTime syncedAt) { this.syncedAt = syncedAt; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public void markAsSynced() {
        this.syncStatus = SyncStatus.SYNCED;
        this.syncedAt = LocalDateTime.now();
    }

    public void markAsFailed(String error) {
        this.syncStatus = SyncStatus.FAILED;
        this.lastError = error;
        incrementRetryCount();
    }
}