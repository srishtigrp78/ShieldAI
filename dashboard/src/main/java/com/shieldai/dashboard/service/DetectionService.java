package com.shieldai.dashboard.service;

import com.shieldai.dashboard.entity.DetectionEntity;
import com.shieldai.dashboard.repository.DetectionRepository;
import com.shieldai.shared.DetectionReport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DetectionService {

private final DetectionRepository repository;
private final NotificationService notificationService;

@Autowired(required = false)
@Lazy
private OfflineDetectionService offlineDetectionService;

public DetectionService(DetectionRepository repository, NotificationService notificationService) {
this.repository = repository;
this.notificationService = notificationService;
}

public void saveDetection(DetectionReport report) {
try {
    DetectionEntity entity = new DetectionEntity();
    entity.setCandidateId(report.getCandidateId());
    entity.setToolName(report.getToolName());
    entity.setToolType(report.getToolType());
    entity.setTimestamp(report.getTimestamp());
    entity.setOsInfo(report.getOsInfo());
    entity.setProcessDetails(report.getProcessDetails());
    entity.setConfidence(report.getConfidence());
    entity.setDescription(report.getDescription());

    repository.save(entity);

    // Create notification for high-confidence detections
    if (report.getConfidence() > 0.8) {
        String message = String.format("High confidence detection: %s (%.0f%% confidence) detected on %s", 
            report.getToolName(), report.getConfidence() * 100, report.getOsInfo());
        notificationService.createNotification(message);
    }
    
    System.out.println("✅ Detection saved to database: " + report.getCandidateId());
} catch (DataAccessException e) {
    System.err.println("Database unavailable, storing offline: " + e.getMessage());
    if (offlineDetectionService != null) {
        offlineDetectionService.storeDetectionOffline(report);
    } else {
        System.err.println("Offline storage not available - detection lost!");
        throw e;
    }
}
}

public List<DetectionReport> getAllDetections() {
    return repository.findAll().stream()
            .map(this::toReport)
            .collect(Collectors.toList());
}

public List<DetectionReport> getFilteredDetections(String candidateId, String startDate, String endDate, String toolName, String search) {
    return getFilteredDetections(candidateId, startDate, endDate, toolName, search, 0, 100);
}

public List<DetectionReport> getFilteredDetections(String candidateId, String startDate, String endDate, String toolName, String search, int page, int size) {
    List<DetectionEntity> entities = repository.findAll();
    
    // Apply filters first to reduce dataset
    List<DetectionReport> filtered = entities.stream()
            .filter(entity -> candidateId == null || candidateId.equals(entity.getCandidateId()))
            .filter(entity -> startDate == null || entity.getTimestamp().compareTo(startDate) >= 0)
            .filter(entity -> endDate == null || entity.getTimestamp().compareTo(endDate) <= 0)
            .filter(entity -> toolName == null || toolName.equalsIgnoreCase(entity.getToolName()))
            .filter(entity -> search == null || 
                    entity.getToolName().toLowerCase().contains(search.toLowerCase()) ||
                    entity.getProcessDetails().toLowerCase().contains(search.toLowerCase()) ||
                    entity.getDescription().toLowerCase().contains(search.toLowerCase()))
            .map(this::toReport)
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())) // Sort by newest first
            .skip((long) page * size)
            .limit(size)
            .collect(Collectors.toList());
    
    return filtered;
}

public List<String> getUniqueToolNames() {
    // Cache this result or use database query for better performance
    List<String> toolNames = repository.findAll().stream()
            .map(DetectionEntity::getToolName)
            .filter(name -> name != null && !name.trim().isEmpty())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    
    // Add fallback tools if empty
    if (toolNames.isEmpty()) {
        return List.of("ChatGPT", "Claude", "GitHub Copilot", "Gemini");
    }
    
    return toolNames;
}

private DetectionReport toReport(DetectionEntity entity) {
    DetectionReport report = new DetectionReport();
    report.setCandidateId(entity.getCandidateId());
    report.setToolName(entity.getToolName());
    report.setToolType(entity.getToolType());
    report.setTimestamp(entity.getTimestamp());
    report.setOsInfo(entity.getOsInfo());
    report.setProcessDetails(entity.getProcessDetails());
    report.setConfidence(entity.getConfidence());
    report.setDescription(entity.getDescription());
    return report;
}

public long getTotalDetections() {
    return repository.count();
}

public long getTotalCandidates() {
    return repository.findAll().stream()
            .map(DetectionEntity::getCandidateId)
            .distinct()
            .count();
}

public long getHighConfidenceDetections() {
    return repository.findAll().stream()
            .filter(d -> d.getConfidence() > 0.9)
            .count();
}
}