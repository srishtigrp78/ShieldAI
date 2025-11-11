package com.shieldai.agent.analytics;

import com.shieldai.shared.DetectionReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AnalyticsManager {
    private final AnalyticsEngine analyticsEngine;
    private final RiskPredictor riskPredictor;
    private final AlertManager alertManager;
    private final ScheduledExecutorService scheduler;
    private final ObjectMapper objectMapper;
    
    public AnalyticsManager() {
        this.analyticsEngine = new AnalyticsEngine();
        this.riskPredictor = new RiskPredictor();
        this.alertManager = new AlertManager();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.objectMapper = new ObjectMapper();
        
        // Schedule periodic analysis
        startPeriodicAnalysis();
    }
    
    public void processDetection(DetectionReport report) {
        // Process through analytics engine
        analyticsEngine.processDetection(report);
        
        // Generate risk prediction
        List<DetectionReport> history = getDetectionHistory(report.getCandidateId());
        if (history.size() >= 3) {
            RiskPredictor.RiskPrediction prediction = riskPredictor.predictRisk(report.getCandidateId(), history);
            alertManager.processRiskPrediction(prediction);
        }
    }
    
    private List<DetectionReport> getDetectionHistory(String candidateId) {
        // This would typically fetch from a database or cache
        // For now, we'll use the analytics engine's internal storage
        return new ArrayList<>(); // Placeholder
    }
    
    private void startPeriodicAnalysis() {
        // Run risk analysis every 5 minutes
        scheduler.scheduleAtFixedRate(this::performPeriodicAnalysis, 5, 5, TimeUnit.MINUTES);
        
        // Generate analytics reports every hour
        scheduler.scheduleAtFixedRate(this::generateAnalyticsReport, 60, 60, TimeUnit.MINUTES);
    }
    
    private void performPeriodicAnalysis() {
        try {
            Map<String, Double> riskScores = analyticsEngine.getRiskScores();
            List<String> highRiskCandidates = analyticsEngine.getHighRiskCandidates(0.7);
            
            if (!highRiskCandidates.isEmpty()) {
                System.out.println("HIGH RISK ALERT: " + highRiskCandidates.size() + " candidates detected");
                highRiskCandidates.forEach(id -> 
                    System.out.println("  - " + id + " (risk: " + riskScores.get(id) + ")")
                );
            }
        } catch (Exception e) {
            System.err.println("Error in periodic analysis: " + e.getMessage());
        }
    }
    
    private void generateAnalyticsReport() {
        try {
            AnalyticsReport report = new AnalyticsReport();
            report.timestamp = new Date();
            report.totalCandidates = analyticsEngine.getRiskScores().size();
            report.highRiskCount = analyticsEngine.getHighRiskCandidates(0.7).size();
            report.mediumRiskCount = analyticsEngine.getHighRiskCandidates(0.4).size() - report.highRiskCount;
            report.patterns = analyticsEngine.getRiskScores().size(); // Simplified
            
            String json = objectMapper.writeValueAsString(report);
            System.out.println("Analytics Report: " + json);
            
        } catch (Exception e) {
            System.err.println("Error generating analytics report: " + e.getMessage());
        }
    }
    
    public Map<String, Double> getCurrentRiskScores() {
        return analyticsEngine.getRiskScores();
    }
    
    public List<String> getHighRiskCandidates(double threshold) {
        return analyticsEngine.getHighRiskCandidates(threshold);
    }
    
    public Map<String, RiskPredictor.RiskPrediction> getRiskPredictions() {
        return riskPredictor.getAllPredictions();
    }
    
    public List<AlertManager.Alert> getActiveAlerts() {
        return alertManager.getActiveAlerts();
    }
    
    public List<AlertManager.Alert> getCriticalAlerts() {
        return alertManager.getAlertsAboveLevel(AlertManager.AlertLevel.HIGH);
    }
    
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    private static class AnalyticsReport {
        public Date timestamp;
        public int totalCandidates;
        public int highRiskCount;
        public int mediumRiskCount;
        public int patterns;
    }
}