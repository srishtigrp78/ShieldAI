package com.shieldai.agent.analytics;

import com.shieldai.shared.DetectionReport;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class PatternDetector {
    private final Map<String, List<Pattern>> detectedPatterns = new HashMap<>();
    
    public void analyzePatterns(String candidateId, List<DetectionReport> reports) {
        List<Pattern> patterns = new ArrayList<>();
        
        // Time-based patterns
        patterns.addAll(detectTimePatterns(reports));
        
        // Tool usage patterns
        patterns.addAll(detectToolPatterns(reports));
        
        // Confidence patterns
        patterns.addAll(detectConfidencePatterns(reports));
        
        detectedPatterns.put(candidateId, patterns);
    }
    
    private List<Pattern> detectTimePatterns(List<DetectionReport> reports) {
        List<Pattern> patterns = new ArrayList<>();
        
        // Burst detection - multiple detections in short time
        Map<String, Long> hourlyCount = reports.stream()
            .collect(Collectors.groupingBy(
                r -> getHourBucket(r.getTimestamp()),
                Collectors.counting()
            ));
        
        hourlyCount.entrySet().stream()
            .filter(entry -> entry.getValue() > 5)
            .forEach(entry -> patterns.add(new Pattern(
                "BURST_ACTIVITY",
                "High activity burst: " + entry.getValue() + " detections in hour " + entry.getKey(),
                0.8
            )));
        
        return patterns;
    }
    
    private List<Pattern> detectToolPatterns(List<DetectionReport> reports) {
        List<Pattern> patterns = new ArrayList<>();
        
        // Tool switching pattern
        Map<String, Long> toolCounts = reports.stream()
            .collect(Collectors.groupingBy(
                DetectionReport::getToolName,
                Collectors.counting()
            ));
        
        if (toolCounts.size() > 3) {
            patterns.add(new Pattern(
                "TOOL_SWITCHING",
                "Multiple tools detected: " + String.join(", ", toolCounts.keySet()),
                0.7
            ));
        }
        
        return patterns;
    }
    
    private List<Pattern> detectConfidencePatterns(List<DetectionReport> reports) {
        List<Pattern> patterns = new ArrayList<>();
        
        // Escalating confidence
        if (reports.size() >= 3) {
            List<Double> confidences = reports.stream()
                .map(DetectionReport::getConfidence)
                .collect(Collectors.toList());
            
            boolean escalating = true;
            for (int i = 1; i < confidences.size(); i++) {
                if (confidences.get(i) <= confidences.get(i-1)) {
                    escalating = false;
                    break;
                }
            }
            
            if (escalating) {
                patterns.add(new Pattern(
                    "ESCALATING_CONFIDENCE",
                    "Confidence levels increasing over time",
                    0.9
                ));
            }
        }
        
        return patterns;
    }
    
    private String getHourBucket(String timestamp) {
        try {
            Instant instant = Instant.parse(timestamp);
            return instant.truncatedTo(ChronoUnit.HOURS).toString();
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    public Map<String, List<Pattern>> getAllPatterns() {
        return new HashMap<>(detectedPatterns);
    }
    
    public static class Pattern {
        private final String type;
        private final String description;
        private final double severity;
        
        public Pattern(String type, String description, double severity) {
            this.type = type;
            this.description = description;
            this.severity = severity;
        }
        
        public String getType() { return type; }
        public String getDescription() { return description; }
        public double getSeverity() { return severity; }
    }
}