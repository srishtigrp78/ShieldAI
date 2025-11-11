package com.shieldai.agent.analytics;

import com.shieldai.shared.DetectionReport;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AnalyticsEngine {
    private final Map<String, List<DetectionReport>> detectionHistory = new ConcurrentHashMap<>();
    private final Map<String, Double> riskScores = new ConcurrentHashMap<>();
    private final PatternDetector patternDetector = new PatternDetector();
    
    public void processDetection(DetectionReport report) {
        String key = report.getCandidateId();
        detectionHistory.computeIfAbsent(key, k -> new ArrayList<>()).add(report);
        
        // Update risk score
        double riskScore = calculateRiskScore(key);
        riskScores.put(key, riskScore);
        
        // Detect patterns
        patternDetector.analyzePatterns(key, detectionHistory.get(key));
    }
    
    private double calculateRiskScore(String candidateId) {
        List<DetectionReport> reports = detectionHistory.get(candidateId);
        if (reports == null || reports.isEmpty()) return 0.0;
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        reports.forEach(r -> stats.addValue(r.getConfidence()));
        
        // Risk factors
        double avgConfidence = stats.getMean();
        double frequency = getRecentFrequency(reports);
        double toolDiversity = getToolDiversity(reports);
        
        return Math.min(1.0, (avgConfidence * 0.4) + (frequency * 0.4) + (toolDiversity * 0.2));
    }
    
    private double getRecentFrequency(List<DetectionReport> reports) {
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        long recentCount = reports.stream()
            .mapToLong(r -> {
                try {
                    return Instant.parse(r.getTimestamp()).isAfter(oneHourAgo) ? 1 : 0;
                } catch (Exception e) {
                    return 0;
                }
            })
            .sum();
        return Math.min(1.0, recentCount / 10.0);
    }
    
    private double getToolDiversity(List<DetectionReport> reports) {
        Set<String> uniqueTools = reports.stream()
            .map(DetectionReport::getToolName)
            .collect(Collectors.toSet());
        return Math.min(1.0, uniqueTools.size() / 5.0);
    }
    
    public Map<String, Double> getRiskScores() {
        return new HashMap<>(riskScores);
    }
    
    public List<String> getHighRiskCandidates(double threshold) {
        return riskScores.entrySet().stream()
            .filter(entry -> entry.getValue() > threshold)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}