package com.shieldai.agent.analytics;

import com.shieldai.shared.DetectionReport;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class RiskPredictor {
    private final Map<String, RiskPrediction> predictions = new HashMap<>();
    
    public RiskPrediction predictRisk(String candidateId, List<DetectionReport> reports) {
        if (reports.size() < 3) {
            return new RiskPrediction(candidateId, 0.0, "Insufficient data", Collections.emptyList());
        }
        
        // Time series analysis
        double trendScore = analyzeTrend(reports);
        
        // Frequency analysis
        double frequencyScore = analyzeFrequency(reports);
        
        // Confidence progression
        double confidenceScore = analyzeConfidenceProgression(reports);
        
        // Combined risk score
        double overallRisk = (trendScore * 0.4) + (frequencyScore * 0.3) + (confidenceScore * 0.3);
        
        List<String> riskFactors = identifyRiskFactors(reports, trendScore, frequencyScore, confidenceScore);
        
        String reasoning = generateReasoning(trendScore, frequencyScore, confidenceScore);
        
        RiskPrediction prediction = new RiskPrediction(candidateId, overallRisk, reasoning, riskFactors);
        predictions.put(candidateId, prediction);
        
        return prediction;
    }
    
    private double analyzeTrend(List<DetectionReport> reports) {
        SimpleRegression regression = new SimpleRegression();
        
        for (int i = 0; i < reports.size(); i++) {
            regression.addData(i, reports.get(i).getConfidence());
        }
        
        double slope = regression.getSlope();
        return Math.max(0.0, Math.min(1.0, slope + 0.5));
    }
    
    private double analyzeFrequency(List<DetectionReport> reports) {
        Instant now = Instant.now();
        
        // Count detections in last hour vs last 24 hours
        long lastHour = reports.stream()
            .mapToLong(r -> {
                try {
                    return Instant.parse(r.getTimestamp()).isAfter(now.minus(1, ChronoUnit.HOURS)) ? 1 : 0;
                } catch (Exception e) {
                    return 0;
                }
            })
            .sum();
        
        long last24Hours = reports.stream()
            .mapToLong(r -> {
                try {
                    return Instant.parse(r.getTimestamp()).isAfter(now.minus(24, ChronoUnit.HOURS)) ? 1 : 0;
                } catch (Exception e) {
                    return 0;
                }
            })
            .sum();
        
        if (last24Hours == 0) return 0.0;
        
        double ratio = (double) lastHour / last24Hours;
        return Math.min(1.0, ratio * 24); // Normalize to 24-hour scale
    }
    
    private double analyzeConfidenceProgression(List<DetectionReport> reports) {
        if (reports.size() < 2) return 0.0;
        
        List<Double> confidences = reports.stream()
            .map(DetectionReport::getConfidence)
            .collect(Collectors.toList());
        
        double avgIncrease = 0.0;
        int increases = 0;
        
        for (int i = 1; i < confidences.size(); i++) {
            double change = confidences.get(i) - confidences.get(i-1);
            if (change > 0) {
                avgIncrease += change;
                increases++;
            }
        }
        
        return increases > 0 ? Math.min(1.0, avgIncrease / increases * 2) : 0.0;
    }
    
    private List<String> identifyRiskFactors(List<DetectionReport> reports, double trend, double frequency, double confidence) {
        List<String> factors = new ArrayList<>();
        
        if (trend > 0.7) factors.add("Increasing detection confidence trend");
        if (frequency > 0.6) factors.add("High recent activity frequency");
        if (confidence > 0.5) factors.add("Escalating confidence levels");
        
        Set<String> tools = reports.stream().map(DetectionReport::getToolName).collect(Collectors.toSet());
        if (tools.size() > 3) factors.add("Multiple detection tools triggered");
        
        return factors;
    }
    
    private String generateReasoning(double trend, double frequency, double confidence) {
        StringBuilder reasoning = new StringBuilder();
        reasoning.append("Risk assessment based on: ");
        reasoning.append(String.format("trend=%.2f, ", trend));
        reasoning.append(String.format("frequency=%.2f, ", frequency));
        reasoning.append(String.format("confidence=%.2f", confidence));
        
        return reasoning.toString();
    }
    
    public Map<String, RiskPrediction> getAllPredictions() {
        return new HashMap<>(predictions);
    }
    
    public static class RiskPrediction {
        private final String candidateId;
        private final double riskScore;
        private final String reasoning;
        private final List<String> riskFactors;
        private final Instant timestamp;
        
        public RiskPrediction(String candidateId, double riskScore, String reasoning, List<String> riskFactors) {
            this.candidateId = candidateId;
            this.riskScore = riskScore;
            this.reasoning = reasoning;
            this.riskFactors = new ArrayList<>(riskFactors);
            this.timestamp = Instant.now();
        }
        
        public String getCandidateId() { return candidateId; }
        public double getRiskScore() { return riskScore; }
        public String getReasoning() { return reasoning; }
        public List<String> getRiskFactors() { return new ArrayList<>(riskFactors); }
        public Instant getTimestamp() { return timestamp; }
        
        public String getRiskLevel() {
            if (riskScore >= 0.8) return "CRITICAL";
            if (riskScore >= 0.6) return "HIGH";
            if (riskScore >= 0.4) return "MEDIUM";
            if (riskScore >= 0.2) return "LOW";
            return "MINIMAL";
        }
    }
}