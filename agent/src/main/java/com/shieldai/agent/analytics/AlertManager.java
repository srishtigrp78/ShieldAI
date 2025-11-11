package com.shieldai.agent.analytics;

import com.shieldai.agent.analytics.RiskPredictor.RiskPrediction;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AlertManager {
    private final Map<String, Alert> activeAlerts = new ConcurrentHashMap<>();
    private final Set<String> notifiedCandidates = ConcurrentHashMap.newKeySet();
    
    public void processRiskPrediction(RiskPrediction prediction) {
        String candidateId = prediction.getCandidateId();
        double riskScore = prediction.getRiskScore();
        
        // Generate alerts based on risk level
        if (riskScore >= 0.8) {
            createAlert(candidateId, AlertLevel.CRITICAL, 
                "Critical risk detected: " + prediction.getRiskLevel(), prediction);
        } else if (riskScore >= 0.6) {
            createAlert(candidateId, AlertLevel.HIGH, 
                "High risk detected: " + prediction.getRiskLevel(), prediction);
        } else if (riskScore >= 0.4) {
            createAlert(candidateId, AlertLevel.MEDIUM, 
                "Medium risk detected: " + prediction.getRiskLevel(), prediction);
        }
    }
    
    private void createAlert(String candidateId, AlertLevel level, String message, RiskPrediction prediction) {
        Alert alert = new Alert(candidateId, level, message, prediction);
        activeAlerts.put(candidateId, alert);
        
        // Notify if not already notified for this candidate
        if (!notifiedCandidates.contains(candidateId)) {
            notifyAlert(alert);
            notifiedCandidates.add(candidateId);
        }
    }
    
    private void notifyAlert(Alert alert) {
        System.out.println("🚨 ALERT [" + alert.level + "] - " + alert.candidateId);
        System.out.println("   Message: " + alert.message);
        System.out.println("   Risk Score: " + String.format("%.2f", alert.prediction.getRiskScore()));
        System.out.println("   Risk Factors: " + String.join(", ", alert.prediction.getRiskFactors()));
        System.out.println("   Reasoning: " + alert.prediction.getReasoning());
        System.out.println();
    }
    
    public List<Alert> getActiveAlerts() {
        return new ArrayList<>(activeAlerts.values());
    }
    
    public List<Alert> getAlertsAboveLevel(AlertLevel minLevel) {
        return activeAlerts.values().stream()
            .filter(alert -> alert.level.ordinal() >= minLevel.ordinal())
            .sorted((a, b) -> b.level.compareTo(a.level))
            .toList();
    }
    
    public void clearAlert(String candidateId) {
        activeAlerts.remove(candidateId);
        notifiedCandidates.remove(candidateId);
    }
    
    public enum AlertLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public static class Alert {
        public final String candidateId;
        public final AlertLevel level;
        public final String message;
        public final RiskPrediction prediction;
        public final Instant timestamp;
        
        public Alert(String candidateId, AlertLevel level, String message, RiskPrediction prediction) {
            this.candidateId = candidateId;
            this.level = level;
            this.message = message;
            this.prediction = prediction;
            this.timestamp = Instant.now();
        }
    }
}